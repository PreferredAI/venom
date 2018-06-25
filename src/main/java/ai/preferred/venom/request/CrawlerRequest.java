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

import java.util.Map;

/**
 * This class allows the removal of proxy from request.
 *
 * @author Maksim Tkachenko
 * @author Ween Jiann Lee
 */
public class CrawlerRequest implements Request, Unwrappable {

  /**
   * An instance of underlying request.
   */
  private final Request inner;

  /**
   * The proxy to be used for this request.
   */
  private HttpHost proxy;

  /**
   * Constructs an instance of crawler request with an underlying
   * request.
   *
   * @param request An instance of the underlying request
   */
  public CrawlerRequest(final Request request) {
    this.inner = request;
    this.proxy = request.getProxy();
  }

  @Override
  public final Method getMethod() {
    return inner.getMethod();
  }

  @Override
  public final String getBody() {
    return inner.getBody();
  }

  @Override
  public final String getUrl() {
    return inner.getUrl();
  }

  @Override
  public final Map<String, String> getHeaders() {
    return inner.getHeaders();
  }

  @Override
  public final HttpHost getProxy() {
    return proxy;
  }

  /**
   * Remove the proxy from this request.
   */
  public final void removeProxy() {
    proxy = null;
  }

  @Override
  public final SleepScheduler getSleepScheduler() {
    return inner.getSleepScheduler();
  }

  @Override
  public final Request getInner() {
    return inner;
  }
}
