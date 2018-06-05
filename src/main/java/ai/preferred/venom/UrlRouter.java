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

package ai.preferred.venom;

import ai.preferred.venom.request.Request;
import ai.preferred.venom.validator.Validator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This class provides an implementation to select a handler based on the url
 * from which they were fetched.
 *
 * @author Maksim Tkachenko
 * @author Ween Jiann Lee
 */
public class UrlRouter implements HandlerRouter, ValidatorRouter {

  private final Handler defaultHandler;

  private final Map<Pattern, Handler> handlerRules = new LinkedHashMap<>();

  private final Map<Pattern, Validator> validatorRules = new LinkedHashMap<>();

  public UrlRouter(Handler defaultHandler) {
    this.defaultHandler = defaultHandler;
  }

  public UrlRouter() {
    defaultHandler = null;
  }

  /**
   * Adds a url pattern, and the handler to be used.
   * <p>
   * Please note that the pattern must be an exact match of the url to work.
   * </p>
   *
   * @param urlPattern regex pattern of the url.
   * @param handler    handler to which the fetched page should use.
   * @return this.
   */
  public UrlRouter register(Pattern urlPattern, Handler handler) {
    handlerRules.put(urlPattern, handler);
    return this;
  }

  /**
   * Adds a url pattern, and the handler to be used.
   * <p>
   * Please note that the pattern must be an exact match of the url to work.
   * </p>
   *
   * @param urlPattern regex pattern of the url.
   * @param validator  validator to which the fetched page should use.
   * @return this.
   */
  public UrlRouter register(Pattern urlPattern, Validator validator) {
    validatorRules.put(urlPattern, validator);
    return this;
  }

  /**
   * Adds a url pattern, and the handler to be used.
   * <p>
   * Please note that the pattern must be an exact match of the url to work.
   * </p>
   *
   * @param urlPattern regex pattern of the url.
   * @param handler    handler to which the fetched page should use.
   * @param validator  validator to which the fetched page should use.
   * @return this.
   */
  public UrlRouter register(Pattern urlPattern, Handler handler, Validator validator) {
    register(urlPattern, handler);
    register(urlPattern, validator);
    return this;
  }

  @Override
  public Handler getHandler(Request request) {
    for (final Map.Entry<Pattern, Handler> rule : handlerRules.entrySet()) {
      if (rule.getKey().matcher(request.getUrl()).matches()) {
        return rule.getValue();
      }
    }

    if (defaultHandler != null) {
      return defaultHandler;
    }

    throw new RuntimeException("Default handler is not set");
  }

  @Override
  public Validator getValidator(Request request) {
    for (final Map.Entry<Pattern, Validator> rule : validatorRules.entrySet()) {
      if (rule.getKey().matcher(request.getUrl()).matches()) {
        return rule.getValue();
      }
    }

    return Validator.ALWAYS_VALID;
  }

}
