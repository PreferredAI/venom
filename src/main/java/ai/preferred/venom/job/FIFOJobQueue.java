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

package ai.preferred.venom.job;

import javax.annotation.Nonnull;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * This class provides and implementation of scheduler with a first in
 * first out queue.
 * <p>
 * Jobs in queue will be processed first in order of insertion.
 * </p>
 *
 * @author Ween Jiann Lee
 */
public class FIFOJobQueue extends AbstractJobQueue {

  /**
   * Constructs an instance of FIFOJobQueue.
   */
  public FIFOJobQueue() {
    super(new LinkedBlockingQueue<>());
  }

  @Override
  public final void put(final @Nonnull Job job) throws InterruptedException {
    getQueue().put(job);
  }

  @Override
  public final boolean offer(final Job job, final long timeout, final @Nonnull TimeUnit unit)
      throws InterruptedException {
    return getQueue().offer(job, timeout, unit);
  }

  @Override
  public final boolean offer(final @Nonnull Job job) {
    return getQueue().offer(job);
  }

  @Override
  public final Job poll(final long timeout, final @Nonnull TimeUnit unit) throws InterruptedException {
    return getQueue().poll(timeout, unit);
  }

  @Override
  public final Job poll() {
    return getQueue().poll();
  }
}
