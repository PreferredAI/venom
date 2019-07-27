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
import javax.validation.constraints.NotNull;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

/**
 * @author Ween Jiann Lee
 * @author Maksim Tkachenko
 */
public abstract class AbstractQueueScheduler extends AbstractQueue<Job> implements QueueScheduler {

  /**
   * The queue used for this scheduler.
   */
  private final BlockingQueue<Job> queue;

  /**
   * The adding part of the scheduler.
   */
  private final Scheduler scheduler;

  /**
   * Constructs an instance of AbstractQueueScheduler.
   *
   * @param queue an instance of BlockingQueue
   */
  protected AbstractQueueScheduler(final BlockingQueue<Job> queue) {
    this.queue = queue;
    this.scheduler = new JobScheduler(this);
  }

  @Override
  public final Scheduler getScheduler() {
    return scheduler;
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

  /**
   * An implementation of ai.preferred.venom.job.Scheduler using Job.
   */
  public static class JobScheduler implements Scheduler {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JobScheduler.class);

    /**
     * The queue used for this scheduler.
     */
    private final QueueScheduler queueScheduler;

    /**
     * Constructs an instance of JobScheduler.
     *
     * @param queueScheduler an instance of BlockingQueue
     */
    public JobScheduler(final QueueScheduler queueScheduler) {
      this.queueScheduler = queueScheduler;
    }

    @Override
    public void add(final @NotNull Request request, final @NotNull Handler handler,
                    final JobAttribute... jobAttributes) {
      final Job job = new Job(request, handler);
      if (jobAttributes != null) {
        for (JobAttribute jobAttribute : jobAttributes) {
          job.addJobAttribute(jobAttribute);
        }
      }
      queueScheduler.add(job);
      LOGGER.debug("Job {} - {} added to queue.", job.toString(), request.getUrl());
    }

    @Override
    public void add(final @NotNull Request request, final JobAttribute... jobAttributes) {
      add(request, null, jobAttributes);
    }

    @Override
    public final void add(final Request request, final Handler handler) {
      add(request, handler, (JobAttribute[]) null);
    }

    @Override
    public void add(final @NotNull Request request) {
      add(request, null, (JobAttribute[]) null);
    }

  }
}
