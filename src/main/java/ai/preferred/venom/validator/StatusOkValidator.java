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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class determines the validity of a response by its status code.
 * <p>
 * A code 200 should return {@code Status.VALID}, or {@code Status.INVALID_CONTENT}
 * otherwise.
 * </p>
 *
 * @author Maksim Tkachenko
 * @author Ween Jiann Lee
 */
public class StatusOkValidator implements Validator {

  /**
   * An instance of this validator.
   */
  public static final StatusOkValidator INSTANCE = new StatusOkValidator();
  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(StatusOkValidator.class);

  @Override
  public final Status isValid(final Request request, final Response response) {
    if (response.getStatusCode() != 200) {
      LOGGER.warn("Status code {} received for {}", response.getStatusCode(), request.getUrl());
      return Status.INVALID_STATUS_CODE;
    }
    return Status.VALID;
  }
}
