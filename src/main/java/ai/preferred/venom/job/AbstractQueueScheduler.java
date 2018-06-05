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

import ai.preferred.venom.Handleable;
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
public abstract class AbstractQueueScheduler
    extends AbstractQueue<Job> implements Scheduler, BlockingQueue<Job>, AutoCloseable {

  abstract BlockingQueue<Job> getQueue();

  @Override
  public void add(Request r, Handleable h, Priority p) {
    add(r, h, p, Priority.FLOOR);
  }

  @Override
  public void add(Request r, Handleable h) {
    add(r, h, Priority.DEFAULT);
  }

  @Override
  public void add(Request r, Priority p, Priority pf) {
    add(r, null, p, pf);
  }

  @Override
  public void add(Request r, Priority p) {
    add(r, null, p, Priority.FLOOR);
  }

  @Override
  public void add(Request r) {
    add(r, null, Priority.DEFAULT, Priority.FLOOR);
  }

  @Nonnull
  @Override
  public Iterator<Job> iterator() {
    return getQueue().iterator();
  }

  @Override
  public int size() {
    return getQueue().size();
  }

  @Nonnull
  @Override
  public Job take() throws InterruptedException {
    return getQueue().take();
  }

  @Override
  public Job poll(long timeout, @Nonnull TimeUnit unit) throws InterruptedException {
    return getQueue().poll(timeout, unit);
  }

  @Override
  public int remainingCapacity() {
    return getQueue().remainingCapacity();
  }

  @Override
  public int drainTo(@Nonnull Collection<? super Job> c) {
    return getQueue().drainTo(c);
  }

  @Override
  public int drainTo(@Nonnull Collection<? super Job> c, int maxElements) {
    return getQueue().drainTo(c, maxElements);
  }

  @Override
  public boolean offer(@Nonnull Job job) {
    return getQueue().offer(job);
  }

  @Override
  public Job poll() {
    return getQueue().poll();
  }

  @Override
  public Job peek() {
    return getQueue().peek();
  }

  @Override
  public void close() {
    getQueue().iterator().forEachRemaining(job -> job.cancel(true));
  }
}
