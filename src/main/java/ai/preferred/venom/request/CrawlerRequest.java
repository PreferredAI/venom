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
 * @author Maksim Tkachenko
 * @author Ween Jiann Lee
 */
public class CrawlerRequest implements Request, Unwrappable {

  private final Request inner;

  private HttpHost proxy;

  public CrawlerRequest(Request request) {
    this.inner = request;
    this.proxy = request.getProxy();
  }

  @Override
  public Method getMethod() {
    return inner.getMethod();
  }

  @Override
  public String getBody() {
    return inner.getBody();
  }

  @Override
  public String getUrl() {
    return inner.getUrl();
  }

  @Override
  public Map<String, String> getHeaders() {
    return inner.getHeaders();
  }

  @Override
  public HttpHost getProxy() {
    return proxy;
  }

  public void removeProxy() {
    proxy = null;
  }

  @Override
  public SleepScheduler getSleepScheduler() {
    return inner.getSleepScheduler();
  }

  @Override
  public Request getInner() {
    return inner;
  }
}
