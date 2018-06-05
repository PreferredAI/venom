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

package ai.preferred.venom.response;

/**
 * This interface represents that the response can be/ has been stored
 *
 * @author Ween Jiann Lee
 */
public interface Retrievable extends Response {

  /**
   * Returns the id of the row where an archive of this response
   * has been insert into a persistent storage
   * <p>This string should be null if no implementation of FileManager
   * is specified during the initialisation of the fetcher</p>
   *
   * @return unique id of where an archive has been saved
   */
  String getSourceId();
}
