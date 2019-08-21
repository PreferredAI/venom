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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;

/**
 * An implementation of ai.preferred.venom.job.Scheduler using Job.
 */
public class JobScheduler implements Scheduler {

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
  public final void add(final @NotNull Request request, final Handler handler,
                        final @NotNull JobAttribute... jobAttributes) {
    final Job job = new Job(request, handler, jobAttributes);
    queueScheduler.add(job);
    LOGGER.debug("Job {} - {} added to queue.", job.toString(), request.getUrl());
  }

  @Override
  public final void add(final @NotNull Request request, final @NotNull JobAttribute... jobAttributes) {
    add(request, null, jobAttributes);
  }

  @Override
  public final void add(final Request request, final @NotNull Handler handler) {
    add(request, handler, new JobAttribute[0]);
  }

  @Override
  public final void add(final @NotNull Request request) {
    add(request, null, new JobAttribute[0]);
  }

  @Override
  public final void add(final @NotNull Request r, final Handler h, final Priority p, final Priority pf) {
    add(r, h, new PriorityJobAttribute(p, pf));
  }

  @Override
  public final void add(final @NotNull Request r, final Handler h, final Priority p) {
    add(r, h, p, Priority.FLOOR);
  }

  @Override
  public final void add(final @NotNull Request r, final Priority p, final Priority pf) {
    add(r, null, p, pf);
  }

  @Override
  public final void add(final @NotNull Request r, final Priority p) {
    add(r, (Handler) null, p);
  }
}
