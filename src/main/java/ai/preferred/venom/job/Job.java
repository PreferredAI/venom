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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * This class will be placed in a scheduler for queuing requests.
 *
 * @author Maksim Tkachenko
 * @author Ween Jiann Lee
 */
public class Job {

  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(Job.class);

  /**
   * The request of this job.
   */
  private final Request request;

  /**
   * The handler of this job.
   */
  private final Handler handler;

  /**
   *
   */
  private final Map<Class<? extends JobAttribute>, JobAttribute> jobAttributeMap = new HashMap<>();

  /**
   * The current try of this job.
   */
  private int tryCount = 1;

  /**
   * Constructs a basic job.
   *
   * @param request       The request of this job.
   * @param handler       The handler of this job.
   * @param jobAttributes attributes to insert to the job.
   */
  public Job(final @NotNull Request request, final Handler handler, final @NotNull JobAttribute... jobAttributes) {
    this.request = request;
    this.handler = handler;
    for (final JobAttribute jobAttribute : jobAttributes) {
      jobAttributeMap.put(jobAttribute.getClass(), jobAttribute);
    }
  }

  /**
   * Constructs a basic job.
   *
   * @param request The request of this job.
   * @param handler The handler of this job.
   */
  public Job(final @NotNull Request request, final Handler handler) {
    this(request, handler, new JobAttribute[0]);
  }

  /**
   * Constructs a basic job.
   *
   * @param request The request of this job.
   */
  public Job(final @NotNull Request request) {
    this(request, null);
  }

  /**
   * Get the request of this job.
   *
   * @return Request of the job.
   */
  @NotNull
  public final Request getRequest() {
    return request;
  }

  /**
   * Get the handler to handle the response of the job.
   * <p>
   * If handler is null, routed handler will be used to assign a
   * handler to the response, based on its criteria.
   * </p>
   *
   * @return Handler for the response or null.
   */
  @Nullable
  public final Handler getHandler() {
    return handler;
  }

  /**
   * Get attempt number of this job.
   *
   * @return Attempt (try) count of the job.
   */
  public final int getTryCount() {
    return tryCount;
  }

  /**
   * This method is called before the job is scheduled
   * for a retry.
   * <p>
   * This method allows you to specify the logic to
   * move the job into its subsequent state for a retry.
   * </p>
   */
  public final void prepareRetry() {
    LOGGER.debug("Preparing job {} - {} for next state.", Integer.toHexString(this.hashCode()), request.getUrl());
    jobAttributeMap.forEach((k, jobAttribute) -> jobAttribute.prepareRetry());
    tryCount++;
  }

  /**
   * Adds or replace the current job attribute if the class of
   * attribute is already present in the map.
   *
   * @param jobAttribute the job attribute to add or replace.
   * @return this.
   */
  public final Job setJobAttribute(final JobAttribute jobAttribute) {
    jobAttributeMap.put(jobAttribute.getClass(), jobAttribute);
    return this;
  }

  /**
   * Get the job attribute for a specific attribute class or
   * return {@code null} if not found.
   *
   * @param clazz the class of attribute to find.
   * @param <T>   the class of attribute to find.
   * @return an instance of job attribute for class or null.
   */
  public final <T extends JobAttribute> T getJobAttribute(final Class<T> clazz) {
    //noinspection unchecked
    return (T) jobAttributeMap.get(clazz);
  }

}
