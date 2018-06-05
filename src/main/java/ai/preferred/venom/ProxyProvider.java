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
import org.apache.http.HttpHost;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Truong Quoc Tuan
 * @author Maksim Tkachenko
 * @author Ween Jiann Lee
 */
public interface ProxyProvider {

  ProxyProvider EMPTY_PROXY_PROVIDER = new ProxyProvider() {
    @Override
    public List<HttpHost> getProxyList() {
      return Collections.emptyList();
    }

    @Override
    public void add(HttpHost proxy) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void addAll(Collection<HttpHost> proxies) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void remove(HttpHost proxy) {
      throw new UnsupportedOperationException();
    }

    @Override
    public HttpHost get(Request request) {
      return null;
    }
  };

  /**
   * Returns a list of all proxies
   *
   * @return list of proxies
   */
  @NotNull
  List<HttpHost> getProxyList();

  /**
   * Add a proxy to the list
   *
   * @param proxy the proxy to be added
   */
  void add(@NotNull HttpHost proxy);

  /**
   * Add a list of proxies to the list
   *
   * @param proxies the list of proxies to be added
   */
  void addAll(@NotNull Collection<HttpHost> proxies);

  /**
   * Remove a proxy from the list
   *
   * @param proxy the proxy to be removed
   */
  void remove(@NotNull HttpHost proxy);

  /**
   * Returns the get proxy from the list
   *
   * @param request the request to be made
   * @return the proxy to use
   */
  @Nullable
  HttpHost get(@NotNull Request request);

}
