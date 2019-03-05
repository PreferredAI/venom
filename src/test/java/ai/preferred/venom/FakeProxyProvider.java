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

public class FakeProxyProvider implements ProxyProvider {

  private final HttpHost proxy = new HttpHost("http://127.0.0.1:8080");

  @Nullable
  @Override
  public HttpHost get(@NotNull Request request) {
    return proxy;
  }
}
