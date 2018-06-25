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

package ai.preferred.venom.request;

import ai.preferred.venom.SleepScheduler;
import org.apache.http.HttpHost;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * @author Maksim Tkachenko
 * @author Truong Quoc Tuan
 * @author Ween Jiann Lee
 */
public interface Request {

  /**
   * Returns the method type of the request.
   *
   * @return method type
   */
  @NotNull
  Method getMethod();

  /**
   * Returns the request body of the request or null if none specified.
   *
   * @return request body
   */
  @Nullable
  String getBody();

  /**
   * Returns the url of the request.
   *
   * @return url
   */
  @NotNull
  String getUrl();

  /**
   * Returns the headers set for the request.
   *
   * @return a map of the headers set
   */
  @NotNull
  Map<String, String> getHeaders();

  /**
   * Returns the proxy set to be used for the request or default to
   * fetcher if none specified.
   *
   * @return proxy
   */
  @Nullable
  HttpHost getProxy();

  /**
   * Returns information about the amount of sleep before this request
   * is made.
   *
   * @return an instance of SleepScheduler
   */
  @Nullable
  SleepScheduler getSleepScheduler();

  /**
   * The method of the request to be made.
   */
  enum Method {

    /**
     * GET method.
     */
    GET,

    /**
     * POST method.
     */
    POST,

    /**
     * HEAD method.
     */
    HEAD,

    /**
     * PUT method.
     */
    PUT,

    /**
     * DELETE method.
     */
    DELETE,

    /**
     * OPTIONS method.
     */
    OPTIONS
  }

}
