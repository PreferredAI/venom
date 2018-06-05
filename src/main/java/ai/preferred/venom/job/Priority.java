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
 * <p>
 * HIGHEST, HIGH, NORMAL, LOW, LOWEST;
 *
 * @author Maksim Tkachenko
 * @author Ween Jiann Lee
 */
public enum Priority {
  HIGHEST,
  HIGH,
  NORMAL,
  LOW,
  LOWEST;

  public static final Priority DEFAULT = NORMAL;
  public static final Priority FLOOR = LOW;

  public int getPriority() {
    return ordinal();
  }

  public Priority downgrade(Priority floor) {
    if (this.equals(floor)) return this;
    if (this.equals(LOWEST)) return this;
    return values()[ordinal() + 1];
  }

  /**
   * Returns the priority one level below the current
   * priority, if priority is higher than the default floor or the
   * lowest available priority. Otherwise return itself.
   *
   * @return Priority after downgrade.
   */
  public Priority downgrade() {
    return downgrade(FLOOR);
  }

}

