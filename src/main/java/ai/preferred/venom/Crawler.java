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

import ai.preferred.venom.fetcher.*;
import ai.preferred.venom.job.Job;
import ai.preferred.venom.job.PriorityJobQueue;
import ai.preferred.venom.job.Scheduler;
import ai.preferred.venom.request.CrawlerRequest;
import ai.preferred.venom.request.Request;
import ai.preferred.venom.response.Response;
import ai.preferred.venom.response.VResponse;
import ai.preferred.venom.validator.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
  private static final Logger LOGGER = LoggerFactory.getLogger(Crawler.class);

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
   * The job queue used.
   */
  @NotNull
  private final BlockingQueue<Job> jobQueue;

  /**
   * The scheduler used.
   */
  @NotNull
  private final Scheduler scheduler;

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
  @Nullable
  private final SleepScheduler sleepScheduler;

  /**
   * The thread pool to fetch requests and execute callbacks.
   */
  @NotNull
  private final ForkJoinPool threadPool;

  /**
   * The worker manager to use.
   */
  @NotNull
  private final WorkerManager workerManager;

  /**
   * A list of pending futures.
   */
  @NotNull
  private final AtomicInteger jobsPending;

  /**
   * The list of fatal exceptions occurred during response handling.
   */
  private final List<FatalHandlerException> fatalHandlerExceptions;

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
    jobQueue = builder.jobQueue;
    scheduler = new Scheduler(jobQueue);
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
    jobsPending = new AtomicInteger();
    fatalHandlerExceptions = Collections.synchronizedList(new ArrayList<>());
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
      if (sleepScheduler != null) {
        sleepTime = sleepScheduler.getSleepTime();
      } else {
        sleepTime = 0;
      }
    } else {
      sleepTime = job.getRequest().getSleepScheduler().getSleepTime();
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
   * Handle a successful response.
   *
   * @param job      The instance of job being processed.
   * @param response Response returned.
   */
  private void handle(final Job job, final Response response) {
    try {
      if (job.getHandler() != null) {
        job.getHandler().handle(job.getRequest(), new VResponse(response), getScheduler(),
            session, workerManager.getWorker());
      } else if (router != null) {
        final Handler routedHandler = router.getHandler(job.getRequest());
        if (routedHandler != null) {
          routedHandler.handle(job.getRequest(), new VResponse(response), getScheduler(),
              session, workerManager.getWorker());
        }
      } else {
        LOGGER.error("No handler to handle request {}.", job.getRequest().getUrl());
      }
    } catch (final FatalHandlerException e) {
      LOGGER.error("Fatal exception occurred in handler, when parsing response ({}), interrupting execution.",
          job.getRequest().getUrl(), e);
      fatalHandlerExceptions.add(e);
    } catch (final Exception e) {
      LOGGER.error("An exception occurred in handler when parsing response: {}", job.getRequest().getUrl(), e);
    } finally {
      jobsPending.decrementAndGet();
    }
  }

  /**
   * Handle all exception thrown during the fetching process.
   *
   * @param job The instance of job being processed.
   * @param ex  Exception returned.
   */
  private void except(final Job job, final Throwable ex) {
    if ((ex instanceof ValidationException && ((ValidationException) ex).getStatus() == Validator.Status.STOP)
        || ex instanceof StopCodeException
        || ex instanceof CancellationException) {
      jobsPending.decrementAndGet();
    } else {
      synchronized (jobsPending) { // Synchronisation required to prevent crawler stopping incorrectly.
        jobsPending.decrementAndGet();
        if (job.getTryCount() < maxTries) {
          job.prepareRetry();
          jobQueue.add(job);
          LOGGER.debug("Job {} - {} re-queued.", Integer.toHexString(job.hashCode()), job.getRequest().getUrl());
        } else {
          LOGGER.error("Max retries reached for request: {}", job.getRequest().getUrl());
        }
      }
    }
  }

  /**
   * Start polling for jobs, and fetch request.
   */
  private void run() {
    fetcher.start();
    long lastRequestTime = 0;
    while (!Thread.currentThread().isInterrupted() && !threadPool.isShutdown() && fatalHandlerExceptions.isEmpty()) {
      try {
        final Job job = jobQueue.poll(100, TimeUnit.MILLISECONDS);
        if (job == null) {
          if (jobsPending.get() > 0) {
            continue;
          }
          // This should only run if pendingJob == 0 && job == null
          synchronized (jobsPending) {
            LOGGER.debug("({}) Checking for exit conditions.", crawlerThread.getName());
            if (jobQueue.peek() == null && jobsPending.get() <= 0 && exitWhenDone.get()) {
              break;
            }
          }
          continue;
        }

        sleep(job, lastRequestTime);
        lastRequestTime = System.nanoTime();

        connections.acquire();
        jobsPending.incrementAndGet();
        threadPool.execute(() -> {
          LOGGER.debug("Preparing job {} - {} (try {}/{}).",
              Integer.toHexString(job.hashCode()), job.getRequest().getUrl(), job.getTryCount(), maxTries);
          final CrawlerRequest crawlerRequest = prepareRequest(job.getRequest(), job.getTryCount());
          if (Thread.currentThread().isInterrupted()) {
            connections.release();
            jobsPending.decrementAndGet();
            LOGGER.debug("The thread pool is interrupted");
            return;
          }

          final CompletableFuture<Response> completableResponseFuture = new CompletableFuture<>();
          completableResponseFuture
              .whenComplete((response, throwable) -> connections.release())
              .thenAcceptAsync(response -> handle(job, response), threadPool)
              .whenComplete((blank, throwable) -> {
                if (throwable != null) {
                  final Throwable cause = throwable.getCause();
                  except(job, cause);
                }
              });

          final Callback callback = new Callback() {
            @Override
            public void completed(final @NotNull Request request, final @NotNull Response response) {
              LOGGER.debug("Completed received for job {} - {}.", Integer.toHexString(job.hashCode()),
                  job.getRequest().getUrl());
              completableResponseFuture.complete(response);
            }

            @Override
            public void failed(final @NotNull Request request, final @NotNull Exception ex) {
              LOGGER.debug("Failed received for job {} - {}.", Integer.toHexString(job.hashCode()),
                  job.getRequest().getUrl());
              completableResponseFuture.completeExceptionally(ex);
            }

            @Override
            public void cancelled(final @NotNull Request request) {
              LOGGER.debug("Cancelled received for job {} - {}.", Integer.toHexString(job.hashCode()),
                  job.getRequest().getUrl());
              completableResponseFuture.cancel(true);
            }
          };

          fetcher.fetch(crawlerRequest, callback);
        });
      } catch (final InterruptedException e) {
        LOGGER.debug("({}) producer thread interrupted.", crawlerThread.getName(), e);
        Thread.currentThread().interrupt();
        break;
      }
    }
    if (!fatalHandlerExceptions.isEmpty()) {
      LOGGER.debug("Handler exception found... Interrupting.");
      interrupt();
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
   * after the jobQueue has reached 0.
   *
   * @return the instance of Crawler used.
   * @throws Exception if this resource cannot be closed.
   */
  public synchronized Crawler startAndClose() throws Exception {
    start();
    close();
    return this;
  }

  /**
   * Interrupts then close this object.
   *
   * @throws Exception if exception is thrown on close.
   */
  public void interruptAndClose() throws Exception {
    interrupt();
    close();
  }

  /**
   * Interrupts crawler, fetcher and worker threads.
   */
  @Override
  public void interrupt() {
    if (!Thread.currentThread().equals(crawlerThread) && crawlerThread.isAlive()) {
      crawlerThread.interrupt();
    }

    if (!threadPool.isTerminated()) {
      threadPool.shutdownNow();
    }

    workerManager.interrupt();

    if (fetcher instanceof Interruptible) {
      ((Interruptible) fetcher).interrupt();
    }
  }

  @Override
  public void close() throws Exception {
    if (exitWhenDone.compareAndSet(false, true)) {
      LOGGER.debug("Initialising \"{}\" shutdown, waiting for threads to join...", crawlerThread.getName());

      try {
        crawlerThread.join();
        LOGGER.debug("{} producer thread joined.", crawlerThread.getName());
      } catch (InterruptedException e) {
        LOGGER.warn("The producer thread joining has been interrupted", e);
        interrupt();
        Thread.currentThread().interrupt();
      }

      threadPool.shutdown();
      try {
        threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        LOGGER.debug("Thread pool has terminated gracefully.");
      } catch (InterruptedException e) {
        LOGGER.warn("The thread pool joining has been interrupted", e);
        interrupt();
        Thread.currentThread().interrupt();
      }

      Exception cachedException = null;
      for (final AutoCloseable closeable : new AutoCloseable[]{workerManager, fetcher}) {
        try {
          closeable.close();
        } catch (final Exception e) {
          if (cachedException != null) {
            cachedException.addSuppressed(e);
          } else {
            cachedException = e;
          }
        }
        if (Thread.currentThread().isInterrupted()) {
          interrupt();
        }
      }

      if (!fatalHandlerExceptions.isEmpty()) {
        final FatalHandlerException mainHandlerException;
        synchronized (fatalHandlerExceptions) {
          final Iterator<FatalHandlerException> iterator = fatalHandlerExceptions.iterator();
          mainHandlerException = iterator.next();
          while (iterator.hasNext()) {
            mainHandlerException.addSuppressed(iterator.next());
          }
          if (cachedException != null) {
            mainHandlerException.addSuppressed(cachedException);
          }
        }
        throw mainHandlerException;
      }

      if (Thread.currentThread().isInterrupted()) {
        Thread.currentThread().interrupt();
      }

      if (cachedException != null) {
        throw cachedException;
      }
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
     * The job queue used.
     */
    private BlockingQueue<Job> jobQueue;

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
      maxConnections = 32;
      maxTries = 50;
      name = "Crawler";
      parallelism = Runtime.getRuntime().availableProcessors();
      workerManager = null;
      propRetainProxy = 0.05;
      router = null;
      jobQueue = new PriorityJobQueue();
      sleepScheduler = new SleepScheduler(250, 2000);
      session = Session.EMPTY_SESSION;
    }

    /**
     * Sets the name for crawler thread.
     *
     * @param name name for crawler thread
     * @return this
     */
    public Builder setName(final @NotNull String name) {
      if (name == null) {
        throw new IllegalStateException("Attribute 'name' cannot be null.");
      }
      this.name = name;
      return this;
    }

    /**
     * Sets the Fetcher to be used, if not set, default will be chosen.
     *
     * @param fetcher fetcher to be used.
     * @return this
     */
    public Builder setFetcher(final @NotNull Fetcher fetcher) {
      if (fetcher == null) {
        throw new IllegalStateException("Attribute 'fetcher' cannot be null.");
      }
      this.fetcher = fetcher;
      return this;
    }

    /**
     * Sets the parallelism level. Defaults to system thread count.
     *
     * @param parallelism the parallelism level.
     * @return this
     */
    public Builder setParallelism(final int parallelism) {
      if (parallelism <= 0) {
        throw new IllegalStateException("Attribute 'parallelism' must be more or equal to 1.");
      }
      this.parallelism = parallelism;
      return this;
    }

    /**
     * Sets the WorkerManager to be used, if not set, default will be chosen.
     *
     * @param workerManager result workerManager to be used.
     * @return this
     */
    public Builder setWorkerManager(final @NotNull WorkerManager workerManager) {
      if (workerManager == null) {
        throw new IllegalStateException("Attribute 'workerManager' cannot be null.");
      }
      this.workerManager = workerManager;
      return this;
    }

    /**
     * Sets the JobQueue to be used, if not set, default will be chosen.
     * This is deprecated, use setJobQueue instead.
     *
     * @param jobQueue scheduler to be used.
     * @return this
     */
    @Deprecated
    public Builder setScheduler(final @NotNull BlockingQueue<Job> jobQueue) {
      if (jobQueue == null) {
        throw new IllegalStateException("Attribute 'jobQueue' cannot be null.");
      }
      this.jobQueue = jobQueue;
      return this;
    }

    /**
     * Sets the JobQueue to be used, if not set, default will be chosen.
     *
     * @param jobQueue scheduler to be used.
     * @return this
     */
    public Builder setJobQueue(final @NotNull BlockingQueue<Job> jobQueue) {
      if (jobQueue == null) {
        throw new IllegalStateException("Attribute 'jobQueue' cannot be null.");
      }
      this.jobQueue = jobQueue;
      return this;
    }

    /**
     * Sets HandlerRouter to be used. Defaults to none.
     *
     * @param router handler router to be used.
     * @return this
     */
    public Builder setHandlerRouter(final HandlerRouter router) {
      this.router = router;
      return this;
    }

    /**
     * The number of concurrent connections allowed out of the client.
     *
     * @param maxConnections maximum number of concurrent connections.
     * @return this
     */
    public Builder setMaxConnections(final int maxConnections) {
      if (maxConnections <= 0) {
        throw new IllegalStateException("Attribute 'maxConnections' must be more or equal to 1.");
      }
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
    public Builder setMaxTries(final int maxTries) {
      if (maxTries <= 0) {
        throw new IllegalStateException("Attribute 'maxTries' must be more or equal to 1.");
      }
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
    public Builder setPropRetainProxy(final double propRetainProxy) {
      if (propRetainProxy > 1 || propRetainProxy < 0) {
        throw new IllegalStateException("Attribute 'propRetainProxy' not within range, must be (0,1].");
      }
      this.propRetainProxy = propRetainProxy;
      return this;
    }

    /**
     * Sets the SleepScheduler to be used, if not set, default will be chosen.
     *
     * @param sleepScheduler sleepAndGetTime scheduler to be used.
     * @return this
     */
    public Builder setSleepScheduler(final SleepScheduler sleepScheduler) {
      this.sleepScheduler = sleepScheduler;
      return this;
    }

    /**
     * Sets the Session to be used, if not set, defaults to {@code Session.EMPTY_SESSION}.
     *
     * @param session Sessions where variables are defined
     * @return this
     */
    public Builder setSession(final Session session) {
      if (session == null) {
        this.session = Session.EMPTY_SESSION;
      }
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

}
