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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Maksim Tkachenko
 * @author Truong Quoc Tuan
 * @author Ween Jiann Lee
 */
public class HttpFetcherRequest implements Request, Unwrappable {

  /**
   * An instance of underlying request.
   */
  private final Request innerRequest;

  /**
   * The headers to append to global headers.
   */
  private final Map<String, String> headers;

  /**
   * The proxy to be used for this request.
   */
  private final HttpHost proxy;

  /**
   * Constructs an instance of http fetcher request.
   *
   * @param innerRequest An instance of underlying request
   */
  public HttpFetcherRequest(final Request innerRequest) {
    this(innerRequest, new HashMap<>(innerRequest.getHeaders()), innerRequest.getProxy());
  }

  /**
   * Constructs an instance of http fetcher request.
   *
   * @param innerRequest An instance of underlying request
   * @param headers      Headers to append to global headers
   * @param proxy        Proxy to be used for this request
   */
  private HttpFetcherRequest(final Request innerRequest, final Map<String, String> headers, final HttpHost proxy) {
    this.innerRequest = innerRequest;
    this.headers = headers;
    this.proxy = proxy;
  }

  /**
   * Prepend headers to the current headers.
   *
   * @param preHeaders Headers to be prepended
   * @return A new instance of http fetcher request
   */
  public final HttpFetcherRequest prependHeaders(final Map<String, String> preHeaders) {
    final Map<String, String> newHeaders = new HashMap<>(headers);
    preHeaders.forEach(newHeaders::putIfAbsent);
    return new HttpFetcherRequest(innerRequest, newHeaders, proxy);
  }

  @Override
  public final Method getMethod() {
    return innerRequest.getMethod();
  }

  @Override
  public final String getBody() {
    return innerRequest.getBody();
  }

  @Override
  public final String getUrl() {
    return innerRequest.getUrl();
  }

  @Override
  public final Map<String, String> getHeaders() {
    return Collections.unmodifiableMap(headers);
  }

  @Override
  public final HttpHost getProxy() {
    return proxy;
  }

  /**
   * Sets proxy to be used for this request.
   *
   * @param proxy Proxy to be used for this request
   * @return A new instance of http fetcher request
   */
  public final HttpFetcherRequest setProxy(final HttpHost proxy) {
    return new HttpFetcherRequest(innerRequest, headers, proxy);
  }

  @Override
  public final SleepScheduler getSleepScheduler() {
    return innerRequest.getSleepScheduler();
  }

  @Override
  public final Request getInner() {
    return innerRequest;
  }
}
