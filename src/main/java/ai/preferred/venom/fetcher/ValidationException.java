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

package ai.preferred.venom.fetcher;

import ai.preferred.venom.response.Response;
import ai.preferred.venom.validator.Validator;

/**
 * @author Ween Jiann Lee
 */
public class ValidationException extends Exception {

  /**
   * The validation status of the response.
   */
  private final Validator.Status status;

  /**
   * The response.
   */
  private final Response response;

  /**
   * Constructs a validation exception with a message.
   *
   * @param status   The validation status of the response.
   * @param response The response validated
   * @param message  A message about the exception
   */
  public ValidationException(final Validator.Status status, final Response response, final String message) {
    super(message);
    this.status = status;
    this.response = response;
  }

  /**
   * @param status   The validation status of the response.
   * @param response The response validated
   * @param message  A message about the exception
   * @param cause    The cause of the exception
   */
  public ValidationException(final Validator.Status status, final Response response, final String message,
                             final Throwable cause) {
    super(message, cause);
    this.status = status;
    this.response = response;
  }

  /**
   * @param status   The validation status of the response.
   * @param response The response validated
   * @param cause    The cause of the exception
   */
  public ValidationException(final Validator.Status status, final Response response, final Throwable cause) {
    super(cause);
    this.status = status;
    this.response = response;
  }

  /**
   * @param status             The validation status of the response.
   * @param response           The response validated
   * @param message            A message about the exception
   * @param cause              The cause of the exception
   * @param enableSuppression  Enable suppression.
   * @param writableStackTrace Enable writable stack trace.
   */
  public ValidationException(final Validator.Status status, final Response response, final String message,
                             final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
    this.status = status;
    this.response = response;
  }

  /**
   * Get the validation status of the response.
   *
   * @return Validation status of the response
   */
  public final Validator.Status getStatus() {
    return status;
  }

  /**
   * Get the response validated.
   *
   * @return Response validated
   */
  public final Response getResponse() {
    return response;
  }

  @Override
  public final Throwable fillInStackTrace() {
    return this;
  }
}
