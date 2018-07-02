/*
 * Copyright 2018 Preferred.AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.preferred.venom;

import ai.preferred.venom.fetcher.AsyncFetcher;
import ai.preferred.venom.fetcher.Fetcher;
import ai.preferred.venom.fetcher.StopCodeException;
import ai.preferred.venom.job.AbstractQueueScheduler;
import ai.preferred.venom.job.Job;
import ai.preferred.venom.job.PriorityQueueScheduler;
import ai.preferred.venom.job.Scheduler;
import ai.preferred.venom.request.CrawlerRequest;
import ai.preferred.venom.request.Request;
import ai.preferred.venom.response.Response;
import org.apache.http.concurrent.FutureCallback;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class handles the coordination between classes during the pre and
 * post fetching of a page such as executing threads, calling to fetcher
 * and manipulating the priority of a scheduled request.
 *
 * @author Maksim Tkachenko
 * @author Truong Quoc Tuan
 * @author Ween Jiann Lee
 */
public final class Crawler implements Interruptible, AutoCloseable {

  /**
   * Logger.
   */
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Crawler.class);

  /**
   * A new thread where the crawler would run.
   */
  @NotNull
  private final Thread crawlerThread;

  /**
   * Allow the crawler to be closed when done.
   */
  @NotNull
  private final AtomicBoolean exitWhenDone;

  /**
   * The fetcher used.
   */
  @NotNull
  private final Fetcher fetcher;

  /**
   * The maximum number of tries for a request.
   */
  private final int maxTries;

  /**
   * The proportion of tries to retain a specified proxy.
   */
  private final double propRetainProxy;

  /**
   * The router to be used.
   */
  @Nullable
  private final HandlerRouter router;

  /**
   * The scheduler used.
   */
  @NotNull
  private final AbstractQueueScheduler scheduler;

  /**
   * The maximum number of simultaneous connections.
   */
  @NotNull
  private final Semaphore connections;

  /**
   * The session store used.
   */
  @NotNull
  private final Session session;

  /**
   * The sleep scheduler used.
   */
  @NotNull
  private final SleepScheduler sleepScheduler;

  /**
   * The thread pool to fetch requests and execute callbacks.
   */
  @NotNull
  private final ExecutorService threadPool;

  /**
   * The worker manager to use.
   */
  @NotNull
  private final WorkerManager workerManager;

  /**
   * A list of pending futures.
   */
  @NotNull
  private final Map<Job, Future> pendingJobs;

  /**
   * Constructs a new instance of crawler.
   *
   * @param builder An instance of builder
   */
  private Crawler(final Builder builder) {
    crawlerThread = new Thread(this::run, builder.name);
    exitWhenDone = new AtomicBoolean(false);
    fetcher = builder.fetcher;
    maxTries = builder.maxTries;
    propRetainProxy = builder.propRetainProxy;
    router = builder.router;
    scheduler = builder.scheduler;
    connections = new Semaphore(builder.maxConnections);
    session = builder.session;
    sleepScheduler = builder.sleepScheduler;
    threadPool = new ForkJoinPool(builder.parallelism,
        pool -> {
          final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
          worker.setName(builder.name + " " + worker.getPoolIndex());
          return worker;
        },
        null,
        true
    );
    workerManager = builder.workerManager == null ? new ThreadedWorkerManager(threadPool) : builder.workerManager;
    pendingJobs = new ConcurrentHashMap<>();
  }

  /**
   * Creates a new instance of Builder.
   *
   * @return an instance of Builder.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builds a new default instance of Crawler.
   *
   * @return an instance of Crawler.
   */
  public static Crawler buildDefault() {
    return builder().build();
  }

  /**
   * Sleep if last request time is less than required sleep time.
   *
   * @param job             An instance of job
   * @param lastRequestTime The time of last request
   * @throws InterruptedException If sleep is interrupted
   */
  private void sleep(final Job job, final long lastRequestTime) throws InterruptedException {
    final long sleepTime;
    if (job.getRequest().getSleepScheduler() == null) {
      sleepTime = sleepScheduler.getSleepTime();
    } else if (job.getRequest().getSleepScheduler() != null) {
      sleepTime = job.getRequest().getSleepScheduler().getSleepTime();
    } else {
      sleepTime = 0;
    }

    final long timeElapsed = System.nanoTime() - lastRequestTime;
    final long timeElapsedMillis = TimeUnit.NANOSECONDS.toMillis(timeElapsed);
    if (sleepTime > timeElapsedMillis) {
      Thread.sleep(sleepTime - timeElapsedMillis);
    }
  }

  /**
   * Check if request is an instance of crawler request and return it
   * if true, otherwise wrap it with crawler request and return that.
   *
   * @param request An instance of request
   * @return An instance of crawler request
   */
  private CrawlerRequest normalizeRequest(final Request request) {
    if (request instanceof CrawlerRequest) {
      return (CrawlerRequest) request;
    }
    return new CrawlerRequest(request);
  }

  /**
   * Normalise request and check if specified proxy should be used.
   *
   * @param request  An instance of request
   * @param tryCount Current try count
   * @return An instance of crawler request
   */
  private CrawlerRequest prepareRequest(final Request request, final int tryCount) {
    final CrawlerRequest crawlerRequest = normalizeRequest(request);
    if (request.getProxy() != null && ((double) tryCount) / maxTries > propRetainProxy) {
      crawlerRequest.removeProxy();
    }
    return crawlerRequest;
  }

  /**
   * Start polling for jobs, and fetch request.
   */
  private void run() {
    fetcher.start();
    long lastRequestTime = 0;
    while (!Thread.currentThread().isInterrupted() && !threadPool.isShutdown()) {
      try {
        final Job job = scheduler.poll(5, TimeUnit.SECONDS);
        if (job == null) {
          synchronized (pendingJobs) {
            LOGGER.debug("({}) Checking for exit conditions.", crawlerThread.getName());
            if (scheduler.peek() == null && pendingJobs.size() == 0 && exitWhenDone.get()) {
              break;
            }
          }
          continue;
        }

        sleep(job, lastRequestTime);
        lastRequestTime = System.nanoTime();

        connections.acquire();
        threadPool.execute(() -> {
          LOGGER.debug("Preparing to fetch {}", job.getRequest().getUrl());
          final CrawlerRequest crawlerRequest = prepareRequest(job.getRequest(), job.getTryCount());
          final Future<Response> responseFuture = fetcher.fetch(crawlerRequest,
              new AsyncCrawlerCallbackProcessor(this, job));
          synchronized (job) {
            pendingJobs.put(job, responseFuture);
            job.notifyAll();
          }
        });
      } catch (InterruptedException e) {
        LOGGER.debug("({}) producer thread interrupted.", crawlerThread.getName(), e);
        break;
      }
    }
    LOGGER.debug("({}) will stop producing requests.", crawlerThread.getName());
  }

  /**
   * Get the instance of scheduler used.
   *
   * @return the instance of scheduler used.
   */
  public Scheduler getScheduler() {
    return scheduler;
  }

  /**
   * Starts the crawler by starting a new thread to poll for jobs.
   *
   * @return the instance of Crawler used.
   */
  public synchronized Crawler start() {
    crawlerThread.start();
    LOGGER.info("{} thread started.", crawlerThread.getName());
    return this;
  }

  /**
   * Starts the crawler by starting a new thread to poll for jobs and close it
   * after the queue has reached 0.
   *
   * @return the instance of Crawler used.
   * @throws Exception if this resource cannot be closed.
   */
  public synchronized Crawler startAndClose() throws Exception {
    start();
    close();
    return this;
  }

  @Override
  public void interruptAndClose() throws Exception {
    crawlerThread.interrupt();
    pendingJobs.values().forEach(future -> future.cancel(true));

    threadPool.shutdownNow();

    if (workerManager instanceof Interruptible) {
      ((Interruptible) workerManager).interruptAndClose();
    }

    close();
  }

  @Override
  public void close() throws Exception {
    if (exitWhenDone.compareAndSet(false, true)) {
      LOGGER.debug("Initialising \"{}\" shutdown, waiting for threads to join...", crawlerThread.getName());
      crawlerThread.join();
      LOGGER.debug("{} producer thread joined.", crawlerThread.getName());
      threadPool.shutdown();

      threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
      LOGGER.debug("{} thread pool joined.", crawlerThread.getName());
      LOGGER.debug("{} shutdown completed.", crawlerThread.getName());

      workerManager.close();
      fetcher.close();
    }
  }

  /**
   * A builder for crawler class.
   */
  public static final class Builder {

    /**
     * The fetcher used.
     */
    private Fetcher fetcher;

    /**
     * The maximum number of simultaneous connections.
     */
    private int maxConnections;

    /**
     * The maximum number of tries for a request.
     */
    private int maxTries;

    /**
     * The name of this crawler.
     */
    private String name;

    /**
     * The parallelism level for multithreading.
     */
    private int parallelism;

    /**
     * The worker manager to use.
     */
    private WorkerManager workerManager;

    /**
     * The proportion of tries to retain a specified proxy.
     */
    private double propRetainProxy;

    /**
     * The router to be used.
     */
    private HandlerRouter router;

    /**
     * The scheduler used.
     */
    private AbstractQueueScheduler scheduler;

    /**
     * The sleep scheduler used.
     */
    private SleepScheduler sleepScheduler;

    /**
     * The session store used.
     */
    private Session session;

    /**
     * Constructs an instance of builder with default values.
     */
    private Builder() {
      fetcher = AsyncFetcher.buildDefault();
      maxConnections = Runtime.getRuntime().availableProcessors() * 10;
      maxTries = 50;
      name = "Crawler";
      parallelism = Runtime.getRuntime().availableProcessors();
      workerManager = null;
      propRetainProxy = 0.05;
      router = null;
      scheduler = new PriorityQueueScheduler();
      sleepScheduler = new SleepScheduler(250, 2000);
      session = Session.EMPTY_SESSION;
    }

    /**
     * Sets the name for crawler thread.
     *
     * @param name name for crawler thread
     * @return this
     */
    public Builder name(final @NotNull String name) {
      this.name = name;
      return this;
    }

    /**
     * Sets the Fetcher to be used, if not set, default will be chosen.
     *
     * @param fetcher fetcher to be used.
     * @return this
     */
    public Builder fetcher(final @NotNull Fetcher fetcher) {
      this.fetcher = fetcher;
      return this;
    }

    /**
     * Sets the parallelism level. Defaults to system thread count.
     *
     * @param parallelism the parallelism level.
     * @return this
     */
    public Builder parallism(final int parallelism) {
      if (parallelism <= 0) {
        LOGGER.warn("Attribute 'numThreads' not within range, defaulting to system default.");
      } else {
        this.parallelism = parallelism;
      }
      return this;
    }

    /**
     * Sets the WorkerManager to be used, if not set, default will be chosen.
     *
     * @param workerManager result workerManager to be used.
     * @return this
     */
    public Builder workerManager(final @NotNull WorkerManager workerManager) {
      this.workerManager = workerManager;
      return this;
    }

    /**
     * Sets the Scheduler to be used, if not set, default will be chosen.
     *
     * @param scheduler scheduler to be used.
     * @return this
     */
    public Builder scheduler(final @NotNull AbstractQueueScheduler scheduler) {
      this.scheduler = scheduler;
      return this;
    }

    /**
     * Sets HandlerRouter to be used. Defaults to none.
     *
     * @param router handler router to be used.
     * @return this
     */
    public Builder router(final @NotNull HandlerRouter router) {
      this.router = router;
      return this;
    }

    /**
     * The number of concurrent connections allowed out of the client.
     *
     * @param maxConnections maximum number of concurrent connections.
     * @return this
     */
    public Builder maxConnections(final int maxConnections) {
      this.maxConnections = maxConnections;
      return this;
    }

    /**
     * Sets number of times to retry for a request. This number excludes the first try.
     * Defaults to 50.
     *
     * @param maxTries max retry times.
     * @return this
     */
    public Builder maxTries(final int maxTries) {
      this.maxTries = maxTries;
      return this;
    }

    /**
     * Sets the proportion of max tries where a specified proxy, if specified will be used.
     * Number should be between 0 and 1 inclusive, Defaults to 0.05.
     * <p>
     * This only comes into effect when a specific proxy is set for the request.
     * This proxy set will be overridden beyond this threshold.
     * </p>
     *
     * @param propRetainProxy threshold percentage.
     * @return this
     */
    public Builder propRetainProxy(final double propRetainProxy) {
      if (propRetainProxy > 1 || propRetainProxy < 0) {
        LOGGER.warn("Attribute 'propRetainProxy' not within range, defaulting to 0.05.");
      } else {
        this.propRetainProxy = propRetainProxy;
      }
      return this;
    }

    /**
     * Sets the SleepScheduler to be used, if not set, default will be chosen.
     *
     * @param sleepScheduler sleepAndGetTime scheduler to be used.
     * @return this
     */
    public Builder sleepScheduler(final @NotNull SleepScheduler sleepScheduler) {
      this.sleepScheduler = sleepScheduler;
      return this;
    }

    /**
     * Sets the Session to be used, if not set, defaults to none.
     *
     * @param session Sessions where variables are defined
     * @return this
     */
    public Builder session(final @NotNull Session session) {
      this.session = session;
      return this;
    }

    /**
     * Builds the crawler with the options specified.
     *
     * @return an instance of Crawler
     */
    public Crawler build() {
      return new Crawler(this);
    }

  }

  /**
   * This class methods is executed upon the completion of fetcher.
   */
  public static final class AsyncCrawlerCallbackProcessor implements FutureCallback<Response> {

    /**
     * The instance of crawler used.
     */
    private final Crawler crawler;

    /**
     * The instance of job being processed.
     */
    private final Job job;

    /**
     * Constructs an instance of async crawler callback processor.
     *
     * @param crawler The instance of crawler used
     * @param job     The instance of job being processed
     */
    private AsyncCrawlerCallbackProcessor(final Crawler crawler, final Job job) {
      this.crawler = crawler;
      this.job = job;
    }

    /**
     * Wait for job to be added to uncompleted futures then remove it, this
     * block of code should only run in a new thread and should only be ran
     * after all logic has completed.
     * <p>
     * This synchronisation is for safety.
     * </p>
     */
    private void removeJob() {
      synchronized (job) {
        while (!crawler.pendingJobs.containsKey(job)) {
          try {
            job.wait();
          } catch (InterruptedException e) {
            LOGGER.error("Waiting to remove job is interrupted.", e);
          }
        }
      }

      synchronized (crawler.pendingJobs) {
        crawler.pendingJobs.remove(job);
      }
    }

    @Override
    public void completed(final Response response) {
      crawler.connections.release();
      crawler.threadPool.execute(() -> {
        try {
          if (job.getHandler() != null) {
            job.getHandler().handle(job.getRequest(), response, crawler.scheduler, crawler.session,
                crawler.workerManager.getWorker());
          } else if (crawler.router != null) {
            final Handleable routedHandler = crawler.router.getHandler(job.getRequest());
            if (routedHandler != null) {
              routedHandler.handle(job.getRequest(), response, crawler.scheduler, crawler.session,
                  crawler.workerManager.getWorker());
            }
          } else {
            LOGGER.error("No handler to handle request {}.", job.getRequest().getUrl());
          }
        } catch (Exception e) {
          LOGGER.error("An exception occurred in handler.", e);
        }
        removeJob();
      });
    }

    @Override
    public void failed(final Exception ex) {
      crawler.connections.release();
      crawler.threadPool.execute(() -> {
        if (ex instanceof StopCodeException) {
          removeJob();
        } else {
          synchronized (crawler.pendingJobs) {
            removeJob();
            if (job.getTryCount() < crawler.maxTries) {
              job.reQueue();
            } else {
              LOGGER.error("Max retries reached for request: {}", job.getRequest().getUrl());
            }
          }
        }
      });
    }

    @Override
    public void cancelled() {
      crawler.connections.release();
      crawler.threadPool.execute(this::removeJob);
    }

  }
}
