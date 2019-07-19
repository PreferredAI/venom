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

package ai.preferred.venom.uagent;

/**
 * @author Maksim Tkachenko
 * @author Ween Jiann Lee
 */
public class DefaultUserAgent implements UserAgent {

  @Override
  public final String get() {
    return "Venom/4.1 ("
        + System.getProperty("os.name") + "; "
        + System.getProperty("os.version") + "; "
        + System.getProperty("os.arch")
        + ")";
  }

}
