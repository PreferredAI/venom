/*
 * Copyright 2017 Preferred.AI
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

import org.junit.Assert;
import org.junit.Test;

public class SessionTest {

  @Test
  public void testSession() {
    final Session.Key<Object> TEST_OBJECT_KEY = new Session.Key<>();
    final Session.Key<Object> FAKE_OBJECT_KEY = new Session.Key<>();
    final Object testObject = new Object();
    final Session session = Session.builder()
        .put(TEST_OBJECT_KEY, testObject)
        .build();

    Assert.assertEquals(testObject, session.get(TEST_OBJECT_KEY));
    Assert.assertNull(session.get(FAKE_OBJECT_KEY));
  }

}
