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

import javax.validation.constraints.NotNull;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * @author Maksim Tkachenko
 * @author Ween Jiann Lee
 */
public interface Worker {

  /**
   * Performs the given task inline, and increase available threads in the pool
   * by one for the execution of other tasks.
   * <p>
   * It is imperative to wrap all I/O tasks in this method to prevent
   * starving other parsing tasks from threads.
   * </p>
   *
   * @param task the I/O blocking task to execute
   * @throws NullPointerException if the task is null
   */
  void executeBlockingIO(@NotNull Runnable task);

  /**
   * Submits a value-returning task for execution and returns a
   * Future representing the pending results of the task. The
   * Future's {@code get} method will return the task's result upon
   * successful completion.
   * <br>
   * <br>
   * If you would like to immediately block waiting
   * for a task, you can use constructions of the form
   * {@code result = exec.submit(aCallable).get();}
   *
   * <p>Note: The {@link java.util.concurrent.Executors} class includes a set of methods
   * that can convert some other common closure-like objects,
   * for example, {@link java.security.PrivilegedAction} to
   * {@link Callable} form so they can be submitted.
   *
   * @param task the task to submit
   * @param <T>  the type of the task's result
   * @return a Future representing pending completion of the task
   * @throws java.util.concurrent.RejectedExecutionException if the task cannot be
   *                                                         scheduled for execution
   * @throws NullPointerException                            if the task is null
   */
  @NotNull <T> Future<T> submit(@NotNull Callable<T> task);

  /**
   * Submits a Runnable task for execution and returns a Future
   * representing that task. The Future's {@code get} method will
   * return the given result upon successful completion.
   *
   * @param task   the task to submit
   * @param result the result to return
   * @param <T>    the type of the result
   * @return a Future representing pending completion of the task
   * @throws java.util.concurrent.RejectedExecutionException if the task cannot be
   *                                                         scheduled for execution
   * @throws NullPointerException                            if the task is null
   */
  @NotNull <T> Future<T> submit(@NotNull Runnable task, T result);

  /**
   * Submits a Runnable task for execution and returns a Future
   * representing that task. The Future's {@code get} method will
   * return {@code null} upon <em>successful</em> completion.
   *
   * @param task the task to submit
   * @return a Future representing pending completion of the task
   * @throws java.util.concurrent.RejectedExecutionException if the task cannot be
   *                                                         scheduled for execution
   * @throws NullPointerException                            if the task is null
   */
  @NotNull Future<?> submit(@NotNull Runnable task);

}
