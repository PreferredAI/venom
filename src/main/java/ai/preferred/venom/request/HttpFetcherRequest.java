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

  private final Request innerRequest;

  private final Map<String, String> headers;

  private final HttpHost proxy;

  public HttpFetcherRequest(Request request) {
    this(request, new HashMap<>(request.getHeaders()), request.getProxy());
  }

  private HttpFetcherRequest(Request innerRequest, Map<String, String> headers, HttpHost proxy) {
    this.innerRequest = innerRequest;
    this.headers = headers;
    this.proxy = proxy;
  }

  public HttpFetcherRequest setProxy(HttpHost proxy) {
    return new HttpFetcherRequest(innerRequest, headers, proxy);
  }

  public HttpFetcherRequest prependHeaders(Map<String, String> preHeaders) {
    final Map<String, String> newHeaders = new HashMap<>(headers);
    preHeaders.forEach(newHeaders::putIfAbsent);
    return new HttpFetcherRequest(innerRequest, newHeaders, proxy);
  }

  @Override
  public Method getMethod() {
    return innerRequest.getMethod();
  }

  @Override
  public String getBody() {
    return innerRequest.getBody();
  }

  @Override
  public String getUrl() {
    return innerRequest.getUrl();
  }

  @Override
  public Map<String, String> getHeaders() {
    return Collections.unmodifiableMap(headers);
  }

  @Override
  public HttpHost getProxy() {
    return proxy;
  }

  @Override
  public SleepScheduler getSleepScheduler() {
    return innerRequest.getSleepScheduler();
  }

  @Override
  public Request getInner() {
    return innerRequest;
  }
}
