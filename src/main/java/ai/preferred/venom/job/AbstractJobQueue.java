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
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

/**
 * @author Ween Jiann Lee
 * @author Maksim Tkachenko
 */
@SuppressWarnings("NullableProblems")
public abstract class AbstractJobQueue extends AbstractQueue<Job> implements BlockingQueue<Job> {

  /**
   * The queue used for this scheduler.
   */
  private final BlockingQueue<Job> queue;

  /**
   * Constructs an instance of AbstractJobQueue.
   *
   * @param queue an instance of BlockingQueue
   */
  protected AbstractJobQueue(final BlockingQueue<Job> queue) {
    this.queue = queue;
  }

  @Nonnull
  @Override
  public final Iterator<Job> iterator() {
    return queue.iterator();
  }

  @Override
  public final int size() {
    return queue.size();
  }

  @Nonnull
  @Override
  public final Job take() throws InterruptedException {
    return queue.take();
  }

  @Override
  public final int remainingCapacity() {
    return queue.remainingCapacity();
  }

  @Override
  public final int drainTo(final @Nonnull Collection<? super Job> c) {
    return queue.drainTo(c);
  }

  @Override
  public final int drainTo(final @Nonnull Collection<? super Job> c, final int maxElements) {
    return queue.drainTo(c, maxElements);
  }

  @Override
  public final Job peek() {
    return queue.peek();
  }

  /**
   * Get the BlockingQueue backing this scheduler.
   *
   * @return an instance of BlockingQueue
   */
  protected final BlockingQueue<Job> getQueue() {
    return queue;
  }

}
