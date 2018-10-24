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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * This class provides and implementation of scheduler with a priority
 * sensitive queue.
 * <p>
 * Jobs with higher priority will be processed first.
 * </p>
 *
 * @author Maksim Tkachenko
 * @author Ween Jiann Lee
 */
public class PriorityQueueScheduler extends AbstractQueueScheduler {

  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(PriorityQueueScheduler.class);

  /**
   * The queue used for this scheduler.
   */
  private final PriorityBlockingQueue<Job> queue = new PriorityBlockingQueue<>();

  @Override
  final PriorityBlockingQueue<Job> getQueue() {
    return queue;
  }

  @Override
  public final void add(final Request r, final Handler h, final Priority p, final Priority pf) {
    final Job job = new BasicJob(r, h, p, pf, queue);
    getQueue().add(job);
    LOGGER.debug("Job {} - {} added to queue.", job.toString(), r.getUrl());
  }

  @Override
  public final void put(final @Nonnull Job job) {
    getQueue().put(job);
  }

  @Override
  public final boolean offer(final Job job, final long timeout, final @Nonnull TimeUnit unit) {
    return getQueue().offer(job, timeout, unit);
  }

}
