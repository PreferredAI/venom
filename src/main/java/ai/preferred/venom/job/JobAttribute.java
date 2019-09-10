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

/**
 * This interface represents attributes that can be added
 * to jobs to manipulate the crawling process.
 *
 * @author Ween Jiann Lee
 */
public interface JobAttribute {

  /**
   * This method is called before the job is scheduled
   * for a retry.
   * <p>
   * This method allows you to specify the logic to
   * move the job into its subsequent state for a retry.
   * </p>
   */
  void prepareRetry();

}
