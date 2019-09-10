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

class PriorityJobAttributeTest {

  @Test
  void testPriority() {
    final PriorityJobAttribute priorityJobAttribute = new PriorityJobAttribute(Priority.HIGHEST);
    Assertions.assertEquals(Priority.HIGHEST, priorityJobAttribute.getPriority());

    final PriorityJobAttribute priorityJobAttributeDefault = new PriorityJobAttribute();
    Assertions.assertEquals(Priority.DEFAULT, priorityJobAttributeDefault.getPriority());
  }

  @Test
  void testPriorityFloor() {
    final PriorityJobAttribute priorityJobAttribute = new PriorityJobAttribute(Priority.HIGH);
    Assertions.assertEquals(Priority.HIGH, priorityJobAttribute.getPriority());
    priorityJobAttribute.prepareRetry();
    Assertions.assertEquals(Priority.NORMAL, priorityJobAttribute.getPriority());
    priorityJobAttribute.prepareRetry();
    Assertions.assertEquals(Priority.LOW, priorityJobAttribute.getPriority());
    priorityJobAttribute.prepareRetry();
    Assertions.assertEquals(Priority.LOW, priorityJobAttribute.getPriority());
  }

  @Test
  void testCompare() {
    final PriorityJobAttribute priorityJobAttributeHigh = new PriorityJobAttribute(Priority.HIGH);
    final PriorityJobAttribute priorityJobAttributeLow = new PriorityJobAttribute(Priority.LOW);
    Assertions.assertTrue(priorityJobAttributeHigh.compareTo(priorityJobAttributeLow) < 0);
    Assertions.assertTrue(priorityJobAttributeLow.compareTo(priorityJobAttributeHigh) > 0);

    final PriorityJobAttribute priorityJobAttributeLow2 = new PriorityJobAttribute(Priority.LOW);
    Assertions.assertEquals(0, priorityJobAttributeLow2.compareTo(priorityJobAttributeLow));
  }

}
