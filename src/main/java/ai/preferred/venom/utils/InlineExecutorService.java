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

package ai.preferred.venom.utils;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Ween Jiann Lee
 */
public class InlineExecutorService extends AbstractExecutorService implements ExecutorService {

  private final AtomicBoolean shutdown = new AtomicBoolean(false);

  private final AtomicBoolean terminated = new AtomicBoolean(false);

  private final Lock lock = new ReentrantLock();

  @Override
  public void shutdown() {
    shutdown.compareAndSet(false, true);
  }

  @Nonnull
  @Override
  public final List<Runnable> shutdownNow() {
    return Collections.emptyList();
  }

  @Override
  public final boolean isShutdown() {
    return shutdown.get();
  }

  @Override
  public final boolean isTerminated() {
    return terminated.get();
  }

  @Override
  public final boolean awaitTermination(final long timeout, final @Nonnull TimeUnit unit) throws InterruptedException {
    if (terminated.get()) {
      return true;
    }
    lock.tryLock(timeout, unit);
    return terminated.get();
  }

  @Override
  public final void execute(final @Nonnull Runnable command) {
    if (shutdown.get()) {
      throw new RejectedExecutionException("Executor has been shutdown.");
    } else {
      lock.lock();
      command.run();
      if (shutdown.get()) {
        terminated.compareAndSet(false, true);
      }
    }
  }
}

