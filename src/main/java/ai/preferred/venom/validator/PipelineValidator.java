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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Maksim Tkachenko
 * @author Ween Jiann Lee
 */
public class PipelineValidator implements Validator {

  private static final Logger LOGGER = LoggerFactory.getLogger(PipelineValidator.class);

  private final List<Validator> validators;

  public PipelineValidator(Validator... validators) {
    this.validators = new LinkedList<>(Arrays.asList(validators));
  }

  public PipelineValidator(List<Validator> validators) {
    this.validators = new LinkedList<>(validators);
  }

  @Override
  public Status isValid(Request request, Response response) {
    int i = 0;
    for (final Validator v : validators) {
      final Status status = v.isValid(request, response);
      if (status != Status.VALID) {
        LOGGER.warn("Validator {} failed for {}", v.getClass().getSimpleName(), request.getUrl());
        return status;
      }
      i++;
    }
    return Status.VALID;
  }

}
