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

package ai.preferred.venom.request;

import javax.validation.constraints.NotNull;

/**
 * This interface represents that the request can be unwrapped
 *
 * @author Ween Jiann Lee
 */
public interface Unwrappable extends Request {

  /**
   * Unwrap all wrapped request to an instance of base request.
   *
   * @param request any implementation of {@link Request}.
   * @return the first instance of request not implementing {@link Unwrappable}.
   */
  @NotNull
  static Request unwrapRequest(@NotNull Request request) {
    Request baseRequest = request;
    while (baseRequest instanceof Unwrappable) {
      baseRequest = ((Unwrappable) baseRequest).getInner();
    }

    return baseRequest;
  }

  /**
   * Returns the unwrapped version of this request
   *
   * @return an instance of request
   */
  @NotNull
  Request getInner();

}
