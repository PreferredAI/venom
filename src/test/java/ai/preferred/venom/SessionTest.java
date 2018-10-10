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

public class SessionTest {

  @Test
  public void testSession() {
    final Session.Key<Object> testObjectKey = new Session.Key<>();
    final Session.Key<Object> fakeObjectKey = new Session.Key<>();
    final Object testObject = new Object();
    final Session session = Session.builder()
        .put(testObjectKey, testObject)
        .build();

    Assertions.assertEquals(testObject, session.get(testObjectKey));
    Assertions.assertNull(session.get(fakeObjectKey));
  }

  @Test
  public void testSingletonSession() {
    final Session.Key<Object> key = new Session.Key<>();
    final Object value = new Object();
    final Session session = Session.singleton(key, value);
    Assertions.assertEquals(value, session.get(key));
  }

}
