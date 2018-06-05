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

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BasicJob.class);

  private final Request request;

  private final Handleable handler;

  private final Priority priorityFloor;

  private final Queue<Job> queue;

  private boolean cancelled = false;

  private boolean done = false;

  private Priority priority;

  private int tryCount = 1;

  public BasicJob(Request request, Handleable handler, Priority priority, Priority priorityFloor, Queue<Job> queue) {
    this.request = request;
    this.handler = handler;
    this.priority = priority;
    this.priorityFloor = priorityFloor;
    this.queue = queue;
  }

  private synchronized void interrupt() {
  }

  @Override
  public Request getRequest() {
    return request;
  }

  @Override
  public Handleable getHandler() {
    return handler;
  }

  @Override
  public Priority getPriority() {
    return priority;
  }

  @Override
  public void reQueue() {
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
  public int getTryCount() {
    return tryCount;
  }

  @Override
  public int compareTo(@Nonnull Job o) {
    return priority.compareTo(o.getPriority());
  }

  @Override
  public synchronized boolean cancel(boolean mayInterruptIfRunning) {
    if (cancelled) {
      LOGGER.debug("Job {} - {} cannot be cancelled, already cancelled.", this.toString(), request.getUrl());
      return false;
    } else if (done) {
      LOGGER.debug("Job {} - {} cannot be cancelled, already done.", this.toString(), getRequest().getUrl());
      return false;
    }

    if (!queue.remove(this) && mayInterruptIfRunning) {
      interrupt();
    }

    LOGGER.debug("Job {} - {} cancelled.", this.toString(), getRequest().getUrl());
    done = true;
    return cancelled = true;
  }

  @Override
  public synchronized boolean isCancelled() {
    return cancelled;
  }

  @Override
  public boolean isDone() {
    return done;
  }

  @Override
  public void done() {
    done = true;
    interrupt();
  }

}
