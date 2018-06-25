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

import javax.annotation.Nullable;

/**
 * This interface allows the user to define a user agent string.
 *
 * @author Maksim Tkachenko
 */
public interface UserAgent {

  /**
   * Gets the user agent for a request.
   *
   * @return user agent
   */
  @Nullable
  String get();

}
