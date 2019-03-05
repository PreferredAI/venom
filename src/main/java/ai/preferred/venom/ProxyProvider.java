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

/**
 * This interface allows the user to define proxies to be used for requests.
 *
 * @author Truong Quoc Tuan
 * @author Maksim Tkachenko
 * @author Ween Jiann Lee
 */
public interface ProxyProvider {

  /**
   * An instance of proxy provider without any proxies.
   */
  ProxyProvider EMPTY_PROXY_PROVIDER = request -> null;

  /**
   * Returns the get proxy from the list.
   *
   * @param request the request to be made
   * @return the proxy to use
   */
  @Nullable
  HttpHost get(@NotNull Request request);

}
