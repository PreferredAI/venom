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

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ween Jiann Lee
 */
public class MysqlFetcherRequest implements Request, Unwrappable {

  /**
   * An instance of underlying request.
   */
  private final Request innerRequest;

  /**
   * The headers to append to global headers.
   */
  private final Map<String, String> headers;

  /**
   * Constructs an instance of mysql fetcher request.
   *
   * @param innerRequest An instance of underlying request
   */
  public MysqlFetcherRequest(final Request innerRequest) {
    this(innerRequest, new HashMap<>(innerRequest.getHeaders()));
  }

  /**
   * Constructs an instance of mysql fetcher request.
   *
   * @param innerRequest An instance of underlying request
   * @param headers      Headers to append to global headers
   */
  private MysqlFetcherRequest(final Request innerRequest, final Map<String, String> headers) {
    this.innerRequest = innerRequest;
    this.headers = headers;
  }

  /**
   * Prepend headers to the current headers.
   *
   * @param preHeaders Headers to be prepended
   * @return A new instance of http fetcher request
   */
  public final MysqlFetcherRequest prependHeaders(final Map<String, String> preHeaders) {
    final Map<String, String> newHeaders = new HashMap<>(headers);
    preHeaders.forEach(newHeaders::putIfAbsent);
    return new MysqlFetcherRequest(innerRequest, newHeaders);
  }

  @Override
  public final @NotNull Request.Method getMethod() {
    return innerRequest.getMethod();
  }

  @Override
  public final String getBody() {
    return innerRequest.getBody();
  }

  @Override
  public final @NotNull String getUrl() {
    return innerRequest.getUrl();
  }

  @Override
  public final @NotNull Map<String, String> getHeaders() {
    return Collections.unmodifiableMap(headers);
  }

  @Override
  public final HttpHost getProxy() {
    return innerRequest.getProxy();
  }

  @Override
  public final @NotNull SleepScheduler getSleepScheduler() {
    return innerRequest.getSleepScheduler();
  }

  @Override
  public final Request getInner() {
    return innerRequest;
  }
}
