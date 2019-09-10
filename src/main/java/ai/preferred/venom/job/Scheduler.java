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
import java.util.concurrent.BlockingQueue;

/**
 * This interface represents only the adding part a scheduler.
 *
 * @author Maksim Tkachenko
 * @author Ween Jiann Lee
 */
public class Scheduler {

  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);

  /**
   * The queue used for this scheduler.
   */
  private final BlockingQueue<Job> queue;

  /**
   * Constructs an instance of Scheduler.
   *
   * @param queue an instance of BlockingQueue
   */
  public Scheduler(final BlockingQueue<Job> queue) {
    this.queue = queue;
  }

  /**
   * Adds a request to the queue.
   * <p>
   * This request would be parsed by the handler specified.
   * </p>
   *
   * @param request       request to fetch when dequeued.
   * @param handler       handler to be used to parse the request.
   * @param jobAttributes attributes to insert to the job.
   */
  public final void add(final @NotNull Request request, final Handler handler,
                        final @NotNull JobAttribute... jobAttributes) {
    final Job job = new Job(request, handler, jobAttributes);
    queue.add(job);
    LOGGER.debug("Job {} - {} added to queue.", job.toString(), request.getUrl());
  }

  /**
   * Adds a request to the queue.
   * <p>
   * This request would be parsed by the handler specified.
   * </p>
   *
   * @param request       request to fetch when dequeued.
   * @param jobAttributes attributes to insert to the job.
   */
  public final void add(final @NotNull Request request, final @NotNull JobAttribute... jobAttributes) {
    add(request, null, jobAttributes);
  }

  /**
   * Adds a request to the queue.
   * <p>
   * This request would be parsed by the handler specified.
   * </p>
   *
   * @param request request to fetch when dequeued.
   * @param handler handler to be used to parse the request.
   */
  public final void add(final Request request, final @NotNull Handler handler) {
    add(request, handler, new JobAttribute[0]);
  }

  /**
   * Adds a request to the queue.
   * <p>
   * This request would be parsed by a handler defined in Router
   * or otherwise defined.
   * </p>
   *
   * @param request request to fetch when dequeued.
   */
  public final void add(final @NotNull Request request) {
    add(request, null, new JobAttribute[0]);
  }

  /**
   * Adds a request to the queue. Will be removed in the next release.
   * <p>
   * This request would be parsed by the handler specified, and
   * its priority can be downgraded to a minimum priority specified.
   * </p>
   *
   * @param r  request to fetch when dequeued
   * @param h  handler to be used to parse the request
   * @param p  initial priority of the request
   * @param pf the minimum (floor) priority of this request
   */
  public final void add(final @NotNull Request r, final Handler h, final Priority p, final Priority pf) {
    add(r, h, new PriorityJobAttribute(p, pf));
  }

  /**
   * Adds a request to the queue. Will be removed in the next release.
   * <p>
   * This request would be parsed by the handler specified, and
   * its priority can be downgraded to the default minimum priority.
   * </p>
   *
   * @param r request to fetch when dequeued
   * @param h handler to be used to parse the request
   * @param p initial priority of the request
   */
  public final void add(final @NotNull Request r, final Handler h, final Priority p) {
    add(r, h, p, Priority.FLOOR);
  }

  /**
   * Adds a request to the queue. Will be removed in the next release.
   * <p>
   * This request would be parsed by a handler defined in Router
   * or otherwise, and its priority can be downgraded to a minimum
   * priority specified.
   * </p>
   *
   * @param r  request to fetch when dequeued
   * @param p  initial priority of the request
   * @param pf the minimum (floor) priority of this request
   */
  @Deprecated
  public final void add(final @NotNull Request r, final Priority p, final Priority pf) {
    add(r, null, p, pf);
  }

  /**
   * Adds a request to the queue. Will be removed in the next release.
   * <p>
   * This request would be parsed by a handler defined in Router
   * or otherwise defined, and its priority can be downgraded to the
   * default minimum priority.
   * </p>
   *
   * @param r request to fetch when dequeued
   * @param p initial priority of the request
   */
  @Deprecated
  public final void add(final @NotNull Request r, final Priority p) {
    add(r, (Handler) null, p);
  }
}
