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

import com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Maksim Tkachenko
 */
public class Session {

  public static final Session EMPTY_SESSION = new Session();

  public static Builder builder() {
    return new Builder();
  }

  /**
   * A class representing the key for a session
   *
   * @param <T> specifies the type of the stored value
   */
  public static final class Key<T> {

  }

  /**
   * Builder for Session
   */
  public static class Builder {

    private final Map<Key<?>, Object> map = new HashMap<>();

    /**
     * Adds a session variable into store
     *
     * @param key   an unique name of the session variable
     * @param value the value of the session variable
     * @param <T>   the type of the value of the session variable
     * @return an instance of Builder
     */
    public <T> Builder put(@NotNull Key<T> key, @Nullable T value) {
      map.put(key, value);
      return this;
    }

    public Session build() {
      return new Session(this);
    }

  }

  private final Map<Key<?>, ?> map;

  private Session() {
    this.map = Collections.emptyMap();
  }

  private Session(@NotNull Builder builder) {
    this.map = ImmutableMap.copyOf(builder.map);
  }

  /**
   * Returns the session variable from the store
   *
   * @param key the name of the session variable to retrieve
   * @param <T> the type of the value of the session variable being retrieved
   * @return the value of the session variable stored
   */
  @SuppressWarnings("unchecked")
  public <T> T get(@NotNull Key<T> key) {
    return (T) map.get(key);
  }

}
