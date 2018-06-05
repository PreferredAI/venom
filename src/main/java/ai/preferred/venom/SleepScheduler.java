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

package ai.preferred.venom;

import javax.validation.constraints.NotNull;
import java.util.Random;

/**
 * @author Maksim Tkachenko
 * @author Ween Jiann Lee
 */
public class SleepScheduler {

  private final Random random;
  private final long from;
  private final long to;

  public SleepScheduler(long sleepTime) {
    this(sleepTime, sleepTime, null);
  }

  public SleepScheduler(long from, long to) {
    this(from, to, new Random(System.currentTimeMillis() * 13));
  }

  private SleepScheduler(long from, long to, @NotNull Random random) {
    this.from = from;
    this.to = to;
    this.random = random;
  }

  /**
   * Cause the current thread to sleep an amount of time
   * specified in this class
   *
   * @throws InterruptedException thread has been interrupted before method has finished running
   */
  public void sleep() throws InterruptedException {
    if (from == to) {
      if (from != 0) Thread.sleep(from);
    } else {
      Thread.sleep(random.nextInt((int) (to - from)) + from);
    }
  }

}
