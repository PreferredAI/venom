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
 * @author Maksim Tkachenko
 * @author Ween Jiann Lee
 */
public class EmptyContentValidator implements Validator {

  private static final Logger LOGGER = LoggerFactory.getLogger(EmptyContentValidator.class);

  public static final EmptyContentValidator INSTANCE = new EmptyContentValidator();

  @Override
  public Status isValid(Request request, Response response) {
    if (response.getContent() != null && response.getContent().length > 0) {
      return Status.VALID;
    }
    LOGGER.warn("Empty response received for {}", request.getUrl());
    return Status.INVALID_CONTENT;
  }

}
