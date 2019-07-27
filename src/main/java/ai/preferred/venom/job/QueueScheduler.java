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


import javax.annotation.Nonnull;
import java.util.concurrent.BlockingQueue;

/**
 * This interface represents only the most basic of a scheduler.
 * It imposes no restrictions or particular details on the the
 * type of queue, and allows for different future types to be returned.
 *
 * @author Maksim Tkachenko
 * @author Ween Jiann Lee
 */
public interface QueueScheduler extends BlockingQueue<Job> {

  /**
   * Get the scheduler to add jobs.
   *
   * @return an instance of Scheduler
   */
  Scheduler getScheduler();

  /**
   * Removes a single instance of the specified element from this queue,
   * if it is present then Inserts the specified element into this queue
   * if it is possible to do so immediately without violating capacity
   * restrictions, returning {@code true} upon success and throwing an
   * {@code IllegalStateException} if no space is currently available.
   *
   * @param job element to be removed from this queue, if present and added
   * @throws ClassCastException       if the class of the specified element
   *                                  is incompatible with this queue or if the class
   *                                  of the specified element
   *                                  prevents it from being added to this queue
   * @throws NullPointerException     if the specified element is null
   * @throws IllegalArgumentException if some property of the specified
   *                                  element prevents it from being added to this queue
   */
  @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
  default void removeAndAdd(final @Nonnull Job job) {
    synchronized (job) {
      remove(job);
      add(job);
    }
  }

}
