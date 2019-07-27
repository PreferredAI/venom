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

import javax.annotation.Nonnull;

/**
 * This class provides an implementation of job attribute with comparable
 * priority.
 *
 * @author Ween Jiann Lee
 */
public class PriorityJobAttribute implements JobAttribute, Comparable<PriorityJobAttribute> {

  /**
   * The priority floor of this job.
   */
  private final Priority priorityFloor;

  /**
   * The priority  of this job.
   */
  private Priority priority;

  /**
   * Constructs an instance of PriorityJobAttribute.
   *
   * @param priority      The priority of this job.
   * @param priorityFloor The priority floor of this job.
   */
  public PriorityJobAttribute(final Priority priority, final Priority priorityFloor) {
    this.priority = priority;
    this.priorityFloor = priorityFloor;
  }

  /**
   * Constructs an instance of PriorityJobAttribute.
   *
   * @param priority The priority of this job.
   */
  public PriorityJobAttribute(final Priority priority) {
    this(priority, Priority.FLOOR);
  }

  /**
   * Constructs an instance of PriorityJobAttribute.
   */
  public PriorityJobAttribute() {
    this(Priority.DEFAULT);
  }

  /**
   * Get the priority in this attribute.
   *
   * @return the priority in this attribute.
   */
  public final Priority getPriority() {
    return priority;
  }

  @Override
  public final void prepareRetry() {
    priority = priority.downgrade(priorityFloor);
  }

  @Override
  public final int compareTo(final @Nonnull PriorityJobAttribute job) {
    return priority.compareTo(job.getPriority());
  }
}
