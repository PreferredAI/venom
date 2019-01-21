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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.concurrent.*;

/**
 * @author Maksim Tkachenko
 */
public class ThreadedWorkerManager implements WorkerManager {

  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ThreadedWorkerManager.class);

  /**
   * The executor used to submit tasks.
   */
  @Nullable
  private final ExecutorService executor;

  /**
   * The worker to expose executor methods.
   */
  private final Worker worker;

  /**
   * Constructs a threaded worker manager with a specified executor.
   *
   * @param executor An executor service
   */
  public ThreadedWorkerManager(final ExecutorService executor) {
    this.executor = executor;
    if (executor instanceof ForkJoinPool || executor == null) {
      this.worker = new ForkJoinWorker();
    } else {
      this.worker = new DefaultWorker(executor);
    }
  }

  @Override
  public final Worker getWorker() {
    return worker;
  }

  @Override
  public final void interruptAndClose() {
    if (executor == null) {
      return;
    }
    LOGGER.debug("Forcefully shutting down the worker manager");
    executor.shutdownNow();
    try {
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
      LOGGER.debug("The worker manager has been terminated");
    } catch (final InterruptedException e) {
      LOGGER.warn("Closing has been interrupted", e);
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public final void close() {
    if (executor == null) {
      return;
    }
    LOGGER.debug("Shutting down the worker manager");
    executor.shutdown();
    try {
      if (executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
        LOGGER.debug("The worker manager has been terminated");
      } else {
        executor.shutdownNow();
      }
    } catch (final InterruptedException e) {
      LOGGER.warn("Closing has been interrupted, forcefully shutting down", e);
      executor.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  /**
   * This abstract class exposes the methods to allow submitting tasks for
   * multithreading and implements inline blocking method.
   */
  public abstract static class AbstractManagedBlockingWorker implements Worker {

    @Override
    public final void executeBlockingIO(final @NotNull Runnable task) {
      if (task == null) {
        throw new NullPointerException();
      }
      final ManagedBlockerTask managedBlockerTask = new ManagedBlockerTask(task);
      try {
        ForkJoinPool.managedBlock(managedBlockerTask);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new AssertionError("Exception of unknown cause. Please verify codebase.", e);
      }
    }

  }

  /**
   * This class exposes the methods to allow submitting tasks for multithreading.
   */
  static final class DefaultWorker extends AbstractManagedBlockingWorker {

    /**
     * The executor used to submit tasks.
     */
    private final ExecutorService executor;

    /**
     * Constructs inner worker with a specified executor service.
     *
     * @param executor An instance of executor service
     */
    DefaultWorker(final ExecutorService executor) {
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

  /**
   * This class exposes the methods to allow submitting tasks for multithreading
   * in {@link ForkJoinPool} or {@link ForkJoinPool#commonPool()}.
   */
  static final class ForkJoinWorker extends AbstractManagedBlockingWorker {

    @Override
    public @NotNull <T> Future<T> submit(final @NotNull Callable<T> task) {
      return ForkJoinTask.adapt(task).fork();
    }

    @Override
    public @NotNull <T> Future<T> submit(final @NotNull Runnable task, final T result) {
      return ForkJoinTask.adapt(task, result).fork();
    }

    @Override
    public @NotNull Future<?> submit(final @NotNull Runnable task) {
      return ForkJoinTask.adapt(task).fork();
    }

  }

  /**
   * This class allows extending managed parallelism for tasks running
   * in {@link ForkJoinPool}s.
   */
  static final class ManagedBlockerTask implements ForkJoinPool.ManagedBlocker {

    /**
     * The task to be run.
     */
    private final Runnable task;

    /**
     * {@code true} if task has successfully completed.
     */
    private boolean done = false;

    /**
     * Constructs a managed blocking task.
     *
     * @param task the blocking task
     */
    private ManagedBlockerTask(final Runnable task) {
      this.task = task;
    }

    @Override
    public boolean block() {
      task.run();
      done = true;
      return true;
    }

    @Override
    public boolean isReleasable() {
      return done;
    }
  }

}
