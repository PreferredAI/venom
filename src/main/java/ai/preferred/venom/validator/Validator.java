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

package ai.preferred.venom.validator;

import ai.preferred.venom.request.Request;
import ai.preferred.venom.response.Response;

import javax.validation.constraints.NotNull;

/**
 * @author Maksim Tkachenko
 * @author Truong Quoc Tuan
 * @author Ween Jiann Lee
 */
public interface Validator {

  /**
   * A validator that always return valid.
   */
  Validator ALWAYS_VALID = (request, response) -> Status.VALID;

  /**
   * Method will be called when a response need validation.
   *
   * @param request  request sent to fetch a response
   * @param response response fetched
   * @return the status of validation
   */
  Status isValid(@NotNull Request request, @NotNull Response response);

  /**
   * The allowed return status of validation.
   */
  enum Status {
    /**
     * The response is valid.
     */
    VALID,
    /**
     * The response has invalid content.
     */
    INVALID_CONTENT,
    /**
     * The response is invalid as it is blocked.
     */
    INVALID_BLOCKED,
    /**
     * The response is invalid due to the status code.
     */
    INVALID_STATUS_CODE
  }

}
