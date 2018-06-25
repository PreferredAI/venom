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

import java.util.regex.Pattern;

/**
 * This class determines the validity of a response by its mime type.
 * <p>
 * A mime type that matches the pattern should return {@code Status.INVALID_CONTENT},
 * or {@code Status.VALID} otherwise.
 * </p>
 *
 * @author Maksim Tkachenko
 * @author Ween Jiann Lee
 */
public class MimeTypeValidator implements Validator {

  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(MimeTypeValidator.class);

  /**
   * The pattern the mime type should match.
   */
  private final Pattern regex;

  /**
   * Constructs mime type validator.
   *
   * @param regex A regex string to match valid mime type
   */
  public MimeTypeValidator(final String regex) {
    this(Pattern.compile(regex));
  }

  /**
   * Constructs mime type validator.
   *
   * @param regex A regex pattern to match valid mime type
   */
  public MimeTypeValidator(final Pattern regex) {
    this.regex = regex;
  }

  @Override
  public final Status isValid(final Request request, final Response response) {
    if (regex.matcher(response.getContentType().getMimeType()).matches()) {
      return Status.VALID;
    }
    LOGGER.warn("Invalid ({}) Mime type received for {}", response.getContentType().getMimeType(), request.getUrl());
    return Status.INVALID_CONTENT;
  }

}
