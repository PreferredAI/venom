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

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * This interface represents only the most basic of a job, to
 * be placed in a scheduler or other forms.
 *
 * @author Maksim Tkachenko
 * @author Ween Jiann Lee
 */
public interface Job extends Comparable<Job> {

  /**
   * Get the request of this job.
   *
   * @return Request of the job.
   */
  @NotNull
  Request getRequest();

  /**
   * Get the handler to handle the response of the job.
   * <p>
   * If handler is null, routed handler will be used to assign a
   * handler to the response, based on its criteria.
   * </p>
   *
   * @return Handler for the response or null.
   */
  @Nullable
  Handleable getHandler();

  /**
   * Get the current priority set for this job.
   *
   * @return the current priority of the job.
   */
  Priority getPriority();

  /**
   * Remove any existing in queue, downgrades the priority and
   * adds the job back into queue.
   */
  void reQueue();

  /**
   * Get attempt number of this job.
   *
   * @return Attempt (try) count of the job.
   */
  int getTryCount();

}
