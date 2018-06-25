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

import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.concurrent.*;

/**
 * @author Maksim Tkachenko
 */
public class ThreadedWorkerManager implements WorkerManager, Interruptible {

  /**
   * Logger.
   */
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ThreadedWorkerManager.class);
  /**
   * The executor used to submit tasks.
   */
  private final ExecutorService executor;
  /**
   * The worker to expose executor methods.
   */
  private final Worker worker;

  /**
   * Constructs a fix thread worker with a specified number of threads.
   *
   * @param numThreads Number of threads
   */
  public ThreadedWorkerManager(final int numThreads) {
    this(Executors.newFixedThreadPool(numThreads));
  }

  /**
   * Constructs a threaded worker manager with a specified executor.
   *
   * @param executor An executor service
   */
  public ThreadedWorkerManager(final ExecutorService executor) {
    this.executor = executor;
    this.worker = new InnerWorker(executor);
  }

  @Override
  public final Worker getWorker() {
    return worker;
  }

  @Override
  public final void interruptAndClose() throws InterruptedException {
    executor.shutdownNow();
    close();
  }

  @Override
  public final void close() throws InterruptedException {
    LOGGER.debug("Initialising processor shutdown, waiting for threads to join...");
    executor.shutdown();
    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    LOGGER.debug("Processor thread pool joined.");
    LOGGER.debug("Processor shutdown completed.");
  }

  /**
   * This class exposes the methods to allow submitting tasks for multithreading.
   */
  private static class InnerWorker implements Worker {

    /**
     * The executor used to submit tasks.
     */
    private final ExecutorService executor;

    /**
     * Constructs inner worker with a specified executor service.
     *
     * @param executor An instance of executor service
     */
    InnerWorker(final ExecutorService executor) {
      this.executor = executor;
    }

    @Override
    public @NotNull <T> Future<T> submit(final @NotNull Callable<T> task) {
      return executor.submit(task);
    }

    @Override
    public @NotNull <T> Future<T> submit(final @NotNull Runnable task, final T result) {
      return executor.submit(task, result);
    }

    @Override
    public @NotNull Future<?> submit(final @NotNull Runnable task) {
      return executor.submit(task);
    }
  }

}
