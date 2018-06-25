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
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Queue;

/**
 * @author Ween Jiann Lee
 */
public class BasicJob implements Job {

  /**
   * Logger.
   */
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BasicJob.class);

  /**
   * The request of this job.
   */
  private final Request request;

  /**
   * The handler of this job.
   */
  private final Handleable handler;

  /**
   * The priority floor of this job.
   */
  private final Priority priorityFloor;

  /**
   * The queue for this job.
   */
  private final Queue<Job> queue;

  /**
   * Determines whether the job is cancelled.
   */
  private boolean cancelled = false;

  /**
   * Determines whether the job is done.
   */
  private boolean done = false;

  /**
   * The priority  of this job.
   */
  private Priority priority;

  /**
   * The current try of this job.
   */
  private int tryCount = 1;

  /**
   * Constructs a basic job.
   *
   * @param request       The request of this job.
   * @param handler       The handler of this job.
   * @param priority      The priority of this job.
   * @param priorityFloor The priority floor of this job.
   * @param queue         The queue for this job.
   */
  public BasicJob(final Request request, final Handleable handler, final Priority priority,
                  final Priority priorityFloor, final Queue<Job> queue) {
    this.request = request;
    this.handler = handler;
    this.priority = priority;
    this.priorityFloor = priorityFloor;
    this.queue = queue;
  }

  /**
   * Interrupt the future.
   */
  private synchronized void interrupt() {
  }

  @Override
  public final Request getRequest() {
    return request;
  }

  @Override
  public final Handleable getHandler() {
    return handler;
  }

  @Override
  public final Priority getPriority() {
    return priority;
  }

  @Override
  public final void reQueue() {
    if (cancelled) {
      LOGGER.debug("Job {} - {} is cancelled, will not be re-queued.", this.toString(), request.getUrl());
      return;
    } else if (done) {
      LOGGER.debug("Job {} - {} is done, will not be re-queued.", this.toString(), request.getUrl());
      return;
    }

    queue.remove(this);
    priority = priority.downgrade(priorityFloor);
    tryCount++;
    queue.add(this);

    LOGGER.debug("Job {} - {} re-queued.", this.toString(), request.getUrl());
  }

  @Override
  public final int getTryCount() {
    return tryCount;
  }

  @Override
  public final int compareTo(final @Nonnull Job job) {
    return priority.compareTo(job.getPriority());
  }

  @Override
  public final synchronized boolean cancel(final boolean mayInterruptIfRunning) {
    if (cancelled) {
      LOGGER.debug("Job {} - {} cannot be cancelled, already cancelled.", this.toString(), request.getUrl());
      return false;
    } else if (done) {
      LOGGER.debug("Job {} - {} cannot be cancelled, already done.", this.toString(), getRequest().getUrl());
      return false;
    }

    final boolean removed = queue.remove(this);
    if (!removed && mayInterruptIfRunning) {
      interrupt();
    }

    LOGGER.debug("Job {} - {} cancelled.", this.toString(), getRequest().getUrl());
    done = true;
    cancelled = true;
    return cancelled;
  }

  @Override
  public final synchronized boolean isCancelled() {
    return cancelled;
  }

  @Override
  public final boolean isDone() {
    return done;
  }

  @Override
  public final void done() {
    done = true;
    interrupt();
  }

}
