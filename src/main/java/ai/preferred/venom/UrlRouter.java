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
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

/**
 * This class provides an implementation to select a handler based on the url
 * from which they were fetched.
 *
 * @author Maksim Tkachenko
 * @author Ween Jiann Lee
 */
public class UrlRouter implements HandlerRouter, ValidatorRouter {

  /**
   * The default handler used if pattern does not match any rules.
   */
  private final Handler defaultHandler;

  /**
   * A list of handler rules.
   */
  private final Map<Pattern, Handler> handlerRules = new LinkedHashMap<>();

  /**
   * A list of validator rules.
   */
  private final Map<Pattern, Validator> validatorRules = new LinkedHashMap<>();

  /**
   * A read write lock for handler rules.
   */
  private final ReentrantReadWriteLock handlerRulesLock = new ReentrantReadWriteLock();

  /**
   * A read write lock for validator rules.
   */
  private final ReentrantReadWriteLock validatorRulesLock = new ReentrantReadWriteLock();

  /**
   * Constructs a url router without default handler.
   */
  public UrlRouter() {
    this(null);
  }

  /**
   * Constructs a url router with default handler.
   *
   * @param defaultHandler default handler
   */
  public UrlRouter(final Handler defaultHandler) {
    this.defaultHandler = defaultHandler;
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
  public final UrlRouter register(final Pattern urlPattern, final Handler handler) {
    handlerRulesLock.writeLock().lock();
    try {
      handlerRules.put(urlPattern, handler);
    } finally {
      handlerRulesLock.writeLock().unlock();
    }
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
  public final UrlRouter register(final Pattern urlPattern, final Validator validator) {
    validatorRulesLock.writeLock().lock();
    try {
      validatorRules.put(urlPattern, validator);
    } finally {
      validatorRulesLock.writeLock().unlock();
    }
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
  public final UrlRouter register(final Pattern urlPattern, final Handler handler, final Validator validator) {
    register(urlPattern, handler);
    register(urlPattern, validator);
    return this;
  }

  @Override
  public final Handler getHandler(final Request request) {
    handlerRulesLock.readLock().lock();
    try {
      for (final Map.Entry<Pattern, Handler> rule : handlerRules.entrySet()) {
        if (rule.getKey().matcher(request.getUrl()).matches()) {
          return rule.getValue();
        }
      }
    } finally {
      handlerRulesLock.readLock().unlock();
    }

    if (defaultHandler != null) {
      return defaultHandler;
    }

    throw new RuntimeException("Default handler is not set");
  }

  @Override
  public final Validator getValidator(final Request request) {
    validatorRulesLock.readLock().lock();
    try {
      for (final Map.Entry<Pattern, Validator> rule : validatorRules.entrySet()) {
        if (rule.getKey().matcher(request.getUrl()).matches()) {
          return rule.getValue();
        }
      }
    } finally {
      validatorRulesLock.readLock().unlock();
    }

    return Validator.ALWAYS_VALID;
  }

}
