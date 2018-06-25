/*
 * Copyright 2018 Preferred.AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed max in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.preferred.venom;

import javax.validation.constraints.NotNull;
import java.util.Random;

/**
 * @author Maksim Tkachenko
 * @author Ween Jiann Lee
 */
public class SleepScheduler {

  /**
   * The seed to generate random number.
   */
  private final Random random;

  /**
   * The minimum sleep time.
   */
  private final long min;

  /**
   * The maximum sleep time.
   */
  private final long max;

  /**
   * Constructs a sleep scheduler with fix sleep time.
   *
   * @param sleepTime Sleep time
   */
  public SleepScheduler(final long sleepTime) {
    this(sleepTime, sleepTime, null);
  }

  /**
   * Constructs a sleep scheduler with range of sleep time.
   *
   * @param min Minimum sleep time
   * @param max Maximum sleep time
   */
  public SleepScheduler(final long min, final long max) {
    this(min, max, new Random(System.currentTimeMillis() * 13));
  }

  /**
   * Constructs a sleep scheduler with fix sleep time with a random seed.
   *
   * @param min    Minimum sleep time
   * @param max    Maximum sleep time
   * @param random Random seed
   */
  private SleepScheduler(final long min, final long max, final @NotNull Random random) {
    if (min < 0) {
      throw new IllegalArgumentException("Sleep time cannot be less than 0.");
    }
    this.min = min;
    if (min > max) {
      throw new IllegalArgumentException("Sleep time in \"min\" cannot be greater less than \"max\".");
    }
    this.max = max;
    this.random = random;
  }

  /**
   * Get the amount of time to wait specified in this class.
   *
   * @return interval required
   */
  public final long getSleepTime() {
    if (min == max) {
      return min;
    } else {
      return random.nextInt((int) (max - min)) + min;
    }
  }

}
