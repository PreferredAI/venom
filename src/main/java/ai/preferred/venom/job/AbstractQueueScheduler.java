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

import ai.preferred.venom.Handler;
import ai.preferred.venom.request.Request;

import javax.annotation.Nonnull;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author Ween Jiann Lee
 * @author Maksim Tkachenko
 */
public abstract class AbstractQueueScheduler extends AbstractQueue<Job> implements Scheduler, BlockingQueue<Job> {

  /**
   * Get the queue this instance is using.
   *
   * @return An instance of blocking queue
   */
  abstract BlockingQueue<Job> getQueue();

  @Override
  public final void add(final Request r, final Handler h, final Priority p) {
    add(r, h, p, Priority.FLOOR);
  }

  @Override
  public final void add(final Request r, final Handler h) {
    add(r, h, Priority.DEFAULT);
  }

  @Override
  public final void add(final Request r, final Priority p, final Priority pf) {
    add(r, null, p, pf);
  }

  @Override
  public final void add(final Request r, final Priority p) {
    add(r, null, p, Priority.FLOOR);
  }

  @Override
  public final void add(final Request r) {
    add(r, null, Priority.DEFAULT, Priority.FLOOR);
  }

  @Nonnull
  @Override
  public final Iterator<Job> iterator() {
    return getQueue().iterator();
  }

  @Override
  public final int size() {
    return getQueue().size();
  }

  @Nonnull
  @Override
  public final Job take() throws InterruptedException {
    return getQueue().take();
  }

  @Override
  public Job poll(final long timeout, final @Nonnull TimeUnit unit) throws InterruptedException {
    return getQueue().poll(timeout, unit);
  }

  @Override
  public final int remainingCapacity() {
    return getQueue().remainingCapacity();
  }

  @Override
  public final int drainTo(final @Nonnull Collection<? super Job> c) {
    return getQueue().drainTo(c);
  }

  @Override
  public final int drainTo(final @Nonnull Collection<? super Job> c, final int maxElements) {
    return getQueue().drainTo(c, maxElements);
  }

  @Override
  public final boolean offer(final @Nonnull Job job) {
    return getQueue().offer(job);
  }

  @Override
  public Job poll() {
    return getQueue().poll();
  }

  @Override
  public final Job peek() {
    return getQueue().peek();
  }
}
