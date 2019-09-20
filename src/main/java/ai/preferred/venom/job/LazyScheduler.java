/*
 * Copyright (c) 2019 Preferred.AI
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

package ai.preferred.venom.job;

import ai.preferred.venom.Handler;
import ai.preferred.venom.request.Request;

import java.util.Iterator;

/**
 * Deprecated, will be removed in the next release.
 * Please use LazyPriorityJobQueue instead.
 */
@Deprecated
public class LazyScheduler extends LazyPriorityJobQueue {

  /**
   * Constructs an instance of lazy scheduler with a default handler.
   *
   * @param requests An iterator to obtain requests
   * @param handler  The default handler to use
   */
  public LazyScheduler(final Iterator<Request> requests, final Handler handler) {
    super(requests, handler);
  }

  /**
   * Constructs an instance of lazy scheduler without a default handler.
   *
   * @param requests An iterator to obtain requests
   */
  public LazyScheduler(final Iterator<Request> requests) {
    super(requests);
  }

}
