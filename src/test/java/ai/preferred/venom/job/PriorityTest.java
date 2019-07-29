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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PriorityTest {

  @Test
  void testDowngrade() {
    Assertions.assertEquals(Priority.FLOOR, Priority.FLOOR.downgrade());
    Assertions.assertEquals(Priority.HIGH, Priority.HIGHEST.downgrade());
  }

  @Test
  void testDowngradeFloor() {
    Assertions.assertEquals(Priority.HIGH, Priority.HIGHEST.downgrade(Priority.HIGH));
    Assertions.assertEquals(Priority.HIGH, Priority.HIGH.downgrade(Priority.HIGH));
    Assertions.assertEquals(Priority.LOWEST, Priority.LOW.downgrade(Priority.LOWEST));
    Assertions.assertEquals(Priority.LOWEST, Priority.LOWEST.downgrade(Priority.LOWEST));
  }

}
