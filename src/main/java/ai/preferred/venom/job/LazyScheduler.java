/*
 * Copyright (c) 2019 Preferred.AI
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
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * Deprecated, will be removed in the next release.
 * Please use LazyPriorityQueueScheduler instead
 */
@Deprecated
public class LazyScheduler implements QueueScheduler {

  /**
   * Link
   */
  private final LazyPriorityQueueScheduler lazyPriorityQueueScheduler;

  /**
   * Constructs an instance of lazy scheduler with a default handler.
   *
   * @param requests An iterator to obtain requests
   * @param handler  The default handler to use
   */
  public LazyScheduler(final Iterator<Request> requests, final Handler handler) {
    lazyPriorityQueueScheduler = new LazyPriorityQueueScheduler(requests, handler);
  }

  /**
   * Constructs an instance of lazy scheduler without a default handler.
   *
   * @param requests An iterator to obtain requests
   */
  public LazyScheduler(final Iterator<Request> requests) {
    lazyPriorityQueueScheduler = new LazyPriorityQueueScheduler(requests);
  }

  @Override
  public final Scheduler getScheduler() {
    return lazyPriorityQueueScheduler.getScheduler();
  }

  @Override
  public final boolean add(@Nonnull final Job job) {
    return lazyPriorityQueueScheduler.add(job);
  }

  @Override
  public final boolean offer(@Nonnull final Job job) {
    return lazyPriorityQueueScheduler.offer(job);
  }

  @Override
  public final Job remove() {
    return lazyPriorityQueueScheduler.remove();
  }

  @Override
  public final Job poll() {
    return lazyPriorityQueueScheduler.poll();
  }

  @Override
  public final Job element() {
    return lazyPriorityQueueScheduler.element();
  }

  @Override
  public final Job peek() {
    return lazyPriorityQueueScheduler.peek();
  }

  @Override
  public final void put(@Nonnull final Job job) throws InterruptedException {
    lazyPriorityQueueScheduler.put(job);
  }

  @Override
  public final boolean offer(final Job job, final long timeout, @Nonnull final TimeUnit unit) throws InterruptedException {
    return lazyPriorityQueueScheduler.offer(job, timeout, unit);
  }

  @Override
  public final Job take() throws InterruptedException {
    return lazyPriorityQueueScheduler.take();
  }

  @Override
  public final Job poll(final long timeout, @Nonnull final TimeUnit unit) throws InterruptedException {
    return lazyPriorityQueueScheduler.poll(timeout, unit);
  }

  @Override
  public final int remainingCapacity() {
    return lazyPriorityQueueScheduler.remainingCapacity();
  }

  @Override
  public final boolean remove(final Object o) {
    return lazyPriorityQueueScheduler.remove(o);
  }

  @Override
  public final boolean containsAll(@Nonnull final Collection<?> c) {
    return lazyPriorityQueueScheduler.containsAll(c);
  }

  @Override
  public final boolean addAll(@Nonnull final Collection<? extends Job> c) {
    return lazyPriorityQueueScheduler.addAll(c);
  }

  @Override
  public final boolean removeAll(@Nonnull final Collection<?> c) {
    return lazyPriorityQueueScheduler.removeAll(c);
  }

  @Override
  public final boolean retainAll(@Nonnull final Collection<?> c) {
    return lazyPriorityQueueScheduler.retainAll(c);
  }

  @Override
  public final void clear() {
    lazyPriorityQueueScheduler.clear();
  }

  @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
  @Override
  public final boolean equals(final Object o) {
    return lazyPriorityQueueScheduler.equals(o);
  }

  @Override
  public final int hashCode() {
    return lazyPriorityQueueScheduler.hashCode();
  }

  @Override
  public final int size() {
    return lazyPriorityQueueScheduler.size();
  }

  @Override
  public final boolean isEmpty() {
    return lazyPriorityQueueScheduler.isEmpty();
  }

  @Override
  public final boolean contains(final Object o) {
    return lazyPriorityQueueScheduler.contains(o);
  }

  @Override
  public final Iterator<Job> iterator() {
    return lazyPriorityQueueScheduler.iterator();
  }

  @Override
  public final Object[] toArray() {
    return lazyPriorityQueueScheduler.toArray();
  }

  @SuppressWarnings("SuspiciousToArrayCall")
  @Override
  public final <T> T[] toArray(@Nonnull final T[] a) {
    return lazyPriorityQueueScheduler.toArray(a);
  }

  @Override
  public final int drainTo(@Nonnull final Collection<? super Job> c) {
    return lazyPriorityQueueScheduler.drainTo(c);
  }

  @Override
  public final int drainTo(@Nonnull final Collection<? super Job> c, final int maxElements) {
    return lazyPriorityQueueScheduler.drainTo(c, maxElements);
  }
}
