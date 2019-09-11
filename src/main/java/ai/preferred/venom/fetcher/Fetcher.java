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

package ai.preferred.venom.fetcher;

import ai.preferred.venom.request.Request;
import ai.preferred.venom.response.Response;

import javax.validation.constraints.NotNull;
import java.util.concurrent.Future;

/**
 * This interface represents only the most basic of fetching a request.
 * It imposes no restrictions or particular details on the request execution process
 * and leaves the specifics of proxy management, validation and response status handling
 * up to individual implementations.
 *
 * @author Maksim Tkachenko
 * @author Truong Quoc Tuan
 * @author Ween Jiann Lee
 */
public interface Fetcher extends AutoCloseable {

  /**
   * Fetcher starter.
   */
  void start();

  /**
   * Fetch the desired HTTP page given in {@link Request}.
   *
   * @param request information for the page to fetch.
   * @return Response future
   */
  @NotNull
  Future<Response> fetch(@NotNull Request request);

  /**
   * Fetch the desired HTTP page given in {@link Request}. Executes
   * callback upon completion.
   *
   * @param request  information for the page to fetch.
   * @param callback callback
   * @return Response future
   */
  @NotNull
  Future<Response> fetch(@NotNull Request request, @NotNull Callback callback);

}
