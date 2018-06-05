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
public class Crawler implements Interruptible, AutoCloseable {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Crawler.class);

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
   * A builder for Crawler class
   */
  public static class Builder {

    private Fetcher fetcher;

    private int maxConnections;

    private int maxTries;

    private String name;

    private int parallelism;

    private WorkerManager workerManager;

    private double propRetainProxy;

    private HandlerRouter router;

    private AbstractQueueScheduler scheduler;

    private SleepScheduler sleepScheduler;

    private Session session;

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
     * Sets the name for crawler thread
     *
     * @param name name for crawler thread
     * @return this.
     */
    public Builder name(@NotNull String name) {
      this.name = name;
      return this;
    }

    /**
     * Sets the Fetcher to be used, if not set, default will be chosen.
     *
     * @param fetcher fetcher to be used.
     * @return this.
     */
    public Builder fetcher(@NotNull Fetcher fetcher) {
      this.fetcher = fetcher;
      return this;
    }

    /**
     * Sets the parallelism level. Defaults to system thread count.
     *
     * @param parallelism the parallelism level.
     * @return this.
     */
    public Builder parallism(int parallelism) {
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
     * @return this.
     */
    public Builder workerManager(@NotNull WorkerManager workerManager) {
      this.workerManager = workerManager;
      return this;
    }

    /**
     * Sets the Scheduler to be used, if not set, default will be chosen.
     *
     * @param scheduler scheduler to be used.
     * @return this.
     */
    public Builder scheduler(@NotNull AbstractQueueScheduler scheduler) {
      this.scheduler = scheduler;
      return this;
    }

    /**
     * Sets HandlerRouter to be used. Defaults to none.
     *
     * @param router handler router to be used.
     * @return this.
     */
    public Builder router(@NotNull HandlerRouter router) {
      this.router = router;
      return this;
    }

    /**
     * The number of concurrent connections allowed out of the client.
     *
     * @param maxConnections maximum number of concurrent connections.
     * @return this.
     */
    public Builder maxConnections(int maxConnections) {
      this.maxConnections = maxConnections;
      return this;
    }

    /**
     * Sets number of times to retry for a request. This number excludes the first try.
     * Defaults to 50.
     *
     * @param maxTries max retry times.
     * @return this.
     */
    public Builder maxTries(int maxTries) {
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
     * @return this.
     */
    public Builder propRetainProxy(double propRetainProxy) {
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
     * @param sleepScheduler sleep scheduler to be used.
     * @return this.
     */
    public Builder sleepScheduler(@NotNull SleepScheduler sleepScheduler) {
      this.sleepScheduler = sleepScheduler;
      return this;
    }

    /**
     * Sets the Session to be used, if not set, defaults to none.
     *
     * @param session Sessions where variables are defined.
     * @return this.
     */
    public Builder session(@NotNull Session session) {
      this.session = session;
      return this;
    }

    /**
     * Builds the crawler with the options specified.
     *
     * @return an instance of Crawler.
     */
    public Crawler build() {
      return new Crawler(this);
    }

  }

  @NotNull
  private final Thread crawlerThread;

  @NotNull
  private final AtomicBoolean exitWhenDone;

  @NotNull
  private final Fetcher fetcher;

  private final int maxTries;

  private final double propRetainProxy;

  @Nullable
  private final HandlerRouter router;

  @NotNull
  private final AbstractQueueScheduler scheduler;

  @NotNull
  private final Semaphore connections;

  @NotNull
  private final Session session;

  @NotNull
  private final SleepScheduler sleepScheduler;

  @NotNull
  private final ExecutorService threadPool;

  @NotNull
  private final WorkerManager workerManager;

  @NotNull
  private final Map<Job, Future> uncompletedFutures;

  protected Crawler(Builder builder) {
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
    uncompletedFutures = new ConcurrentHashMap<>();
  }

  public static class AsyncCrawlerCallbackProcessor implements FutureCallback<Response> {

    private final Crawler crawler;

    private final Job job;

    private AsyncCrawlerCallbackProcessor(Crawler crawler, Job job) {
      this.crawler = crawler;
      this.job = job;
    }

    private Response prepareResponse(Response response, Request request) {
      return response;
    }

    @Override
    public void completed(Response unpreparedResponse) {
      cancelled();
      crawler.threadPool.execute(() -> {
        final Response response = prepareResponse(unpreparedResponse, job.getRequest());

        if (job.getHandler() != null) {
          job.getHandler()
              .handle(job.getRequest(), response, crawler.scheduler, crawler.session, crawler.workerManager.getWorker());
          return;
        } else if (crawler.router != null) {
          final Handleable routedHandler = crawler.router.getHandler(job.getRequest());
          if (routedHandler != null) {
            routedHandler
                .handle(job.getRequest(), response, crawler.scheduler, crawler.session, crawler.workerManager.getWorker());
            return;
          }
        }
        LOGGER.error("No handler to handle request {}.", job.getRequest().getUrl());
      });
    }

    @Override
    public void failed(Exception ex) {
      cancelled();
      if (ex instanceof StopCodeException) {
        job.cancel(true);
      } else {
        if (job.getTryCount() <= crawler.maxTries)
          job.reQueue();
      }
    }

    @Override
    public void cancelled() {
      crawler.connections.release();
      crawler.uncompletedFutures.remove(job);
    }

  }

  private CrawlerRequest normalizeRequest(Request request) {
    if (request instanceof CrawlerRequest) {
      return (CrawlerRequest) request;
    }
    return new CrawlerRequest(request);
  }

  private CrawlerRequest prepareRequest(Request request, int tryCount) {
    CrawlerRequest crawlerRequest = normalizeRequest(request);
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

    while (!Thread.currentThread().isInterrupted() && !threadPool.isShutdown()) {
      try {
        final Job job = scheduler.poll(5, TimeUnit.SECONDS);
        if (job == null) {
          if (uncompletedFutures.size() == 0 && exitWhenDone.get()) {
            break;
          }
          continue;
        }

        if (job.getRequest().getSleepScheduler() == null) {
          sleepScheduler.sleep();
        } else if (job.getRequest().getSleepScheduler() != null) {
          job.getRequest().getSleepScheduler().sleep();
        }

        connections.acquire();
        threadPool.execute(() -> {
          LOGGER.debug("Preparing to fetch {}", job.getRequest().getUrl());
          final CrawlerRequest crawlerRequest = prepareRequest(job.getRequest(), job.getTryCount());
          final Future<Response> responseFuture = fetcher.fetch(crawlerRequest,
              new AsyncCrawlerCallbackProcessor(this, job));
          uncompletedFutures.put(job, responseFuture);
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
    uncompletedFutures.values().forEach(future -> future.cancel(true));

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

      scheduler.close();
      threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
      LOGGER.debug("{} thread pool joined.", crawlerThread.getName());
      LOGGER.debug("{} shutdown completed.", crawlerThread.getName());

      workerManager.close();
      fetcher.close();
    }
  }
}
