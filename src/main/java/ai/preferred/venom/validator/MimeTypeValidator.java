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
 * @author Maksim Tkachenko
 * @author Ween Jiann Lee
 */
public class MimeTypeValidator implements Validator {

  private static final Logger LOGGER = LoggerFactory.getLogger(MimeTypeValidator.class);

  private final Pattern regex;

  public MimeTypeValidator(String regex) {
    this(Pattern.compile(regex));
  }

  public MimeTypeValidator(Pattern regex) {
    this.regex = regex;
  }

  @Override
  public Status isValid(Request request, Response response) {
    if (regex.matcher(response.getContentType().getMimeType()).matches()) {
      return Status.VALID;
    }
    LOGGER.warn("Invalid ({}) Mime type received for {}", response.getContentType().getMimeType(), request.getUrl());
    return Status.INVALID_CONTENT;
  }

}
