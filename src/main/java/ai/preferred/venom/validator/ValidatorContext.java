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

/**
 * Allows user to dynamically change validator being used at runtime.
 * <p>
 * Strategy Pattern implementation that adds a Context
 * class that controls which validator is used.
 *
 * @author eriks22
 */
public class ValidatorContext implements Validator {

  private Validator validator;

  /**
   * Create new ValidatorContext.
   * e.g.: ValidatorContext valCon = ValidatorContext(new PipelineValidator());
   *
   * @param validator validator used in form of new ValidatorContext
   */
  public ValidatorContext(final Validator validator) {
    this.validator = validator;
  }

  /**
   * Get the current validator used.
   *
   * @return the current Validator
   */
  public Validator getValidator() {
    return validator;
  }

  /**
   * Defines a new validator to be used.
   * <p>
   * Once a new validator is set, any new request will use
   * this new Validator.
   *
   * @param validator new Validator to use
   */
  public void setValidator(final Validator validator) {
    this.validator = validator;
  }

  @Override
  public Status isValid(Request request, Response response) {
    return validator.isValid(request, response);
  }
}
