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

package ai.preferred.venom.job;

import ai.preferred.venom.Handler;
import ai.preferred.venom.request.Request;

import javax.validation.constraints.NotNull;

/**
 * This interface represents only the adding part a scheduler.
 *
 * @author Maksim Tkachenko
 * @author Ween Jiann Lee
 */
public interface Scheduler {

  /**
   * Adds a request to the queue.
   * <p>
   * This request would be parsed by the handler specified.
   * </p>
   *
   * @param request       request to fetch when dequeued.
   * @param handler       handler to be used to parse the request.
   * @param jobAttributes attributes to insert to the job.
   */
  void add(@NotNull Request request, @NotNull Handler handler, JobAttribute... jobAttributes);

  /**
   * Adds a request to the queue.
   * <p>
   * This request would be parsed by the handler specified.
   * </p>
   *
   * @param request       request to fetch when dequeued.
   * @param jobAttributes attributes to insert to the job.
   */
  void add(@NotNull Request request, JobAttribute... jobAttributes);

  /**
   * Adds a request to the queue.
   * <p>
   * This request would be parsed by the handler specified.
   * </p>
   *
   * @param request request to fetch when dequeued.
   * @param handler handler to be used to parse the request.
   */
  void add(@NotNull Request request, @NotNull Handler handler);

  /**
   * Adds a request to the queue.
   * <p>
   * This request would be parsed by a handler defined in Router
   * or otherwise defined.
   * </p>
   *
   * @param request request to fetch when dequeued.
   */
  void add(@NotNull Request request);

  /**
   * Adds a request to the queue. Will be removed in the next release.
   * <p>
   * This request would be parsed by the handler specified, and
   * its priority can be downgraded to a minimum priority specified.
   * </p>
   *
   * @param r  request to fetch when dequeued
   * @param h  handler to be used to parse the request
   * @param p  initial priority of the request
   * @param pf the minimum (floor) priority of this request
   */
  @Deprecated
  void add(@NotNull Request r, @NotNull Handler h, Priority p, Priority pf);

  /**
   * Adds a request to the queue. Will be removed in the next release.
   * <p>
   * This request would be parsed by the handler specified, and
   * its priority can be downgraded to the default minimum priority.
   * </p>
   *
   * @param r request to fetch when dequeued
   * @param h handler to be used to parse the request
   * @param p initial priority of the request
   */
  @Deprecated
  void add(@NotNull Request r, @NotNull Handler h, Priority p);

  /**
   * Adds a request to the queue. Will be removed in the next release.
   * <p>
   * This request would be parsed by a handler defined in Router
   * or otherwise, and its priority can be downgraded to a minimum
   * priority specified.
   * </p>
   *
   * @param r  request to fetch when dequeued
   * @param p  initial priority of the request
   * @param pf the minimum (floor) priority of this request
   */
  @Deprecated
  void add(@NotNull Request r, Priority p, Priority pf);

  /**
   * Adds a request to the queue. Will be removed in the next release.
   * <p>
   * This request would be parsed by a handler defined in Router
   * or otherwise defined, and its priority can be downgraded to the
   * default minimum priority.
   * </p>
   *
   * @param r request to fetch when dequeued
   * @param p initial priority of the request
   */
  @Deprecated
  void add(@NotNull Request r, Priority p);

}
