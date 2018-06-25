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
import ai.preferred.venom.Handler;
import ai.preferred.venom.request.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * This class provides and implementation of scheduler with a priority
 * sensitive queue and polls from iterator when queue is empty.
 * <p>
 * Jobs in queue will be processed first in order of higher priority,
 * followed by requests in the iterator.
 * </p>
 *
 * @author Maksim Tkachenko
 * @author Ween Jiann Lee
 */
public class LazyScheduler extends AbstractQueueScheduler {

  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(LazyScheduler.class);

  /**
   * The queue used for this scheduler.
   */
  private final PriorityBlockingQueue<Job> queue = new PriorityBlockingQueue<>();

  /**
   * An object to synchronise upon.
   */
  private final Object lock = new Object();

  /**
   * The iterator to draw requests from.
   */
  private final Iterator<Request> requests;

  /**
   * The default handler for this scheduler.
   */
  private final Handler handler;

  /**
   * Constructs an instance of lazy scheduler with a default handler.
   *
   * @param requests An iterator to obtain requests
   * @param handler  The default handler to use
   */
  public LazyScheduler(final Iterator<Request> requests, final Handler handler) {
    this.requests = requests;
    this.handler = handler;
  }

  /**
   * Constructs an instance of lazy scheduler without a default handler.
   *
   * @param requests An iterator to obtain requests
   */
  public LazyScheduler(final Iterator<Request> requests) {
    this(requests, null);
  }

  @Override
  final PriorityBlockingQueue<Job> getQueue() {
    return queue;
  }

  @Override
  public final void add(final Request r, final Handleable h, final Priority p, final Priority pf) {
    Job job = new BasicJob(r, h, p, pf, queue);
    getQueue().add(job);
    LOGGER.debug("Job {} - {} added to queue.", job.toString(), r.getUrl());
  }

  /**
   * Poll request from the iterator.
   *
   * @return An new job instance
   */
  private Job pollLazyRequest() {
    return new BasicJob(requests.next(), handler, Priority.DEFAULT, Priority.FLOOR, getQueue());
  }

  @Override
  public final Job poll() {
    synchronized (lock) {
      if (getQueue().isEmpty() && requests.hasNext()) {
        return pollLazyRequest();
      }
    }
    return getQueue().poll();
  }

  @Override
  public final void put(final @Nonnull Job job) {
    getQueue().put(job);
  }

  @Override
  public final boolean offer(final Job job, final long timeout, final @Nonnull TimeUnit unit) {
    return getQueue().offer(job, timeout, unit);
  }

  @Override
  public final Job poll(final long time, final @Nonnull TimeUnit unit) throws InterruptedException {
    synchronized (lock) {
      if (getQueue().isEmpty() && requests.hasNext()) {
        return pollLazyRequest();
      }
    }
    return getQueue().poll(time, unit);
  }

  @Override
  public final boolean isEmpty() {
    synchronized (lock) {
      return getQueue().isEmpty() && !requests.hasNext();
    }
  }
}
