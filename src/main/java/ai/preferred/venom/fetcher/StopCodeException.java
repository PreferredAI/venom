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

/**
 * @author Ween Jiann Lee
 */
public class StopCodeException extends Exception {

  /**
   * The status code from the response.
   */
  private final int statusCode;

  /**
   * Constructs a stop code exception with a message.
   *
   * @param statusCode The status code received from the response
   * @param message    A message about the exception
   */
  public StopCodeException(final int statusCode, final String message) {
    super(message);
    this.statusCode = statusCode;
  }

  /**
   * Constructs a stop code exception with a message and a cause.
   *
   * @param statusCode The status code received from the response
   * @param message    A message about the exception
   * @param cause      The cause of the exception
   */
  public StopCodeException(final int statusCode, final String message, final Throwable cause) {
    super(message, cause);
    this.statusCode = statusCode;
  }

  /**
   * Constructs a stop code exception with a cause.
   *
   * @param statusCode The status code received from the response
   * @param cause      The cause of the exception
   */
  public StopCodeException(final int statusCode, final Throwable cause) {
    super(cause);
    this.statusCode = statusCode;
  }

  /**
   * Constructs a stop code exception with a message and a cause.
   *
   * @param statusCode         The status code received from the response
   * @param message            A message about the exception
   * @param cause              The cause of the exception
   * @param enableSuppression  Enable suppression.
   * @param writableStackTrace Enable writable stack trace.
   */
  public StopCodeException(final int statusCode, final String message, final Throwable cause,
                           final boolean enableSuppression, final boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
    this.statusCode = statusCode;
  }


  @Override
  public final Throwable fillInStackTrace() {
    return this;
  }
}
