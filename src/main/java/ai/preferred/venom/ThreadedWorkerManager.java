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

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ThreadedWorkerManager.class);

  private static class InnerCollector implements Worker {

    private final ExecutorService executor;

    public InnerCollector(ExecutorService executor) {
      this.executor = executor;
    }

    @Override
    public @NotNull <T> Future<T> submit(@NotNull Callable<T> task) {
      return executor.submit(task);
    }

    @Override
    public @NotNull <T> Future<T> submit(@NotNull Runnable task, T result) {
      return executor.submit(task, result);
    }

    @Override
    public @NotNull Future<?> submit(@NotNull Runnable task) {
      return executor.submit(task);
    }
  }

  private final ExecutorService executor;
  private final Worker collector;

  public ThreadedWorkerManager(int numThreads) {
    this(Executors.newFixedThreadPool(numThreads));
  }

  public ThreadedWorkerManager(ExecutorService executor) {
    this.executor = executor;
    this.collector = new InnerCollector(executor);
  }

  @Override
  public Worker getWorker() {
    return collector;
  }

  @Override
  public void interruptAndClose() throws InterruptedException {
    executor.shutdownNow();
    close();
  }

  @Override
  public void close() throws InterruptedException {
    LOGGER.debug("Initialising processor shutdown, waiting for threads to join...");
    executor.shutdown();
    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    LOGGER.debug("Processor thread pool joined.");
    LOGGER.debug("Processor shutdown completed.");
  }

}
