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

/**
 * Job priorities, list in descending order of priority:
 * HIGHEST, HIGH, NORMAL, LOW, LOWEST.
 *
 * @author Maksim Tkachenko
 * @author Ween Jiann Lee
 */
public enum Priority {

  /**
   * Highest priority.
   */
  HIGHEST,

  /**
   * High priority.
   */
  HIGH,

  /**
   * Normal priority.
   */
  NORMAL,

  /**
   * Low priority.
   */
  LOW,

  /**
   * Lowest priority.
   */
  LOWEST;

  /**
   * The default starting priority for a job.
   */
  public static final Priority DEFAULT = NORMAL;

  /**
   * The default lowest priority for a job.
   */
  public static final Priority FLOOR = LOW;

  /**
   * Returns the priority one level below the current
   * priority if priority is higher than the specified floor or the
   * lowest available priority. Otherwise return itself.
   *
   * @param floor Priority floor
   * @return Priority after downgrade.
   */
  public Priority downgrade(final Priority floor) {
    if (this.compareTo(floor) >= 0) {
      return this;
    }
    return values()[ordinal() + 1];
  }

  /**
   * Returns the priority one level below the current
   * priority if priority is higher than the default floor or the
   * lowest available priority. Otherwise return itself.
   *
   * @return Priority after downgrade.
   */
  public Priority downgrade() {
    return downgrade(FLOOR);
  }

}

