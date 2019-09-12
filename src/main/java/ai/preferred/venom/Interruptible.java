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

/**
 * @author Ween Jiann Lee
 */
public interface Interruptible {

  /**
   * Interrupt the underlying mechanisms of the class.
   * <p>
   * Please note that this {@code interrupt} method should be
   * idempotent.  In other words, calling this {@code interrupt}
   * method more than once should not have any side effect.
   * </p>
   */
  void interrupt();

}
