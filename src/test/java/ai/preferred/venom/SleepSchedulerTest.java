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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SleepSchedulerTest {

  private void checkFixedResult(SleepScheduler sleepScheduler, int result) {
    for (int i = 0; i < 10; i++) {
      Assertions.assertEquals(result, sleepScheduler.getSleepTime());
    }
  }

  private void checkResultWithinBound(SleepScheduler sleepScheduler, int min, int max) {
    final long sleep = sleepScheduler.getSleepTime();
    for (int i = 0; i < 100; i++) {
      Assertions.assertTrue(sleep >= min && sleep <= max);
    }
  }

  private void checkVariableResult(SleepScheduler sleepScheduler, int min, int max) {
    checkResultWithinBound(sleepScheduler, min, max);
    final long sleep = sleepScheduler.getSleepTime();
    for (int i = 0; i < 100; i++) {
      if (sleep != sleepScheduler.getSleepTime()) {
        return;
      }
    }
    throw new RuntimeException("Sleep time not random.");
  }

  @Test
  public void testFixSleepTime() {
    checkFixedResult(new SleepScheduler(0), 0);
    checkFixedResult(new SleepScheduler(10), 10);
    checkFixedResult(new SleepScheduler(10, 10), 10);
  }

  @Test
  public void testVariableSleepTime() {
    checkVariableResult(new SleepScheduler(0, 10), 0, 10);
    checkVariableResult(new SleepScheduler(0, 100), 5, 100);
  }

  @Test
  public void testSleepTimeLessThanZero() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> new SleepScheduler(-1));
  }

  @Test
  public void testSleepTimeMaxGTMin() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> new SleepScheduler(50, 10));
  }

  @Test
  public void testSleepTimeMaxGTMinLTZerio() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> new SleepScheduler(50, -10));
  }

}
