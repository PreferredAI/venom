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
 * @author Maksim Tkachenko
 * @author Ween Jiann Lee
 */
public class LazyScheduler extends AbstractQueueScheduler {

  private static final Logger LOGGER = LoggerFactory.getLogger(LazyScheduler.class);

  private final PriorityBlockingQueue<Job> queue = new PriorityBlockingQueue<>();

  private final Object lock = new Object();

  private final Iterator<Request> requests;
  private final Handler handler;

  public LazyScheduler(Iterator<Request> requests, Handler handler) {
    this.requests = requests;
    this.handler = handler;
  }

  public LazyScheduler(Iterator<Request> requests) {
    this(requests, null);
  }

  @Override
  PriorityBlockingQueue<Job> getQueue() {
    return queue;
  }

  @Override
  public void add(Request r, Handleable h, Priority p, Priority pf) {
    Job job = new BasicJob(r, h, p, pf, queue);
    getQueue().add(job);
    LOGGER.debug("Job {} - {} added to queue.", job.toString(), r.getUrl());
  }

  private Job pollLazyRequest() {
    return new BasicJob(requests.next(), handler, Priority.LOW, Priority.FLOOR, getQueue());
  }

  @Override
  public Job poll() {
    synchronized (lock) {
      if (getQueue().isEmpty() && requests.hasNext()) {
        return pollLazyRequest();
      }
    }
    return getQueue().poll();
  }

  @Override
  public void put(@Nonnull Job job) {
    getQueue().put(job);
  }

  @Override
  public boolean offer(Job job, long timeout, @Nonnull TimeUnit unit) {
    return getQueue().offer(job, timeout, unit);
  }

  @Override
  public Job poll(long time, @Nonnull TimeUnit unit) throws InterruptedException {
    synchronized (lock) {
      if (getQueue().isEmpty() && requests.hasNext()) {
        return pollLazyRequest();
      }
    }
    return getQueue().poll(time, unit);
  }

  @Override
  public boolean isEmpty() {
    synchronized (lock) {
      return getQueue().isEmpty() && !requests.hasNext();
    }
  }
}
