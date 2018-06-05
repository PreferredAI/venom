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

  private final Validator.Status status;

  private final Response response;

  public ValidationException(Validator.Status status, Response response, String message) {
    super(message);
    this.status = status;
    this.response = response;
  }

  public ValidationException(Validator.Status status, Response response, String message, Throwable cause) {
    super(message, cause);
    this.status = status;
    this.response = response;
  }

  public ValidationException(Validator.Status status, Response response, Throwable cause) {
    super(cause);
    this.status = status;
    this.response = response;
  }

  public ValidationException(Validator.Status status, Response response, String message, Throwable cause,
                             boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
    this.status = status;
    this.response = response;
  }

  public Validator.Status getStatus() {
    return status;
  }

  public Response getResponse() {
    return response;
  }

  @Override
  public Throwable fillInStackTrace() {
    return this;
  }
}
