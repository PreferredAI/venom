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
public final class Session {

  /**
   * An instance of an empty session.
   */
  public static final Session EMPTY_SESSION = new Session();

  /**
   * Constructs a singleton session.
   *
   * @param key   an unique identifier of the session variable
   * @param value the value of the session variable
   * @param <T>   the type of the value of the session variable
   * @return An instance of session with single key-value pair
   */
  public static <T> Session singleton(final @NotNull Key<T> key, final @Nullable T value) {
    return new Session(key, value);
  }

  /**
   * A map of all session key and value.
   */
  private final Map<Key<?>, ?> map;

  /**
   * The generic session constructor.
   *
   * @param map the map to replace
   */
  private Session(Map<Key<?>, ?> map) {
    this.map = map;
  }

  /**
   * Constructs an empty session.
   */
  private Session() {
    this(Collections.emptyMap());
  }


  /**
   * Constructs a singleton session.
   *
   * @param key   an unique identifier of the session variable
   * @param value the value of the session variable
   * @param <T>   the type of the value of the session variable
   */
  private <T> Session(final @NotNull Key<T> key, final @Nullable T value) {
    this(Collections.singletonMap(key, value));
  }

  /**
   * Constructs a session with builder variables.
   *
   * @param builder An instance of builder
   */
  private Session(final @NotNull Builder builder) {
    this(ImmutableMap.copyOf(builder.map));
  }

  /**
   * Create a new instance of builder.
   *
   * @return A new instance of builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns the session variable from the store.
   *
   * @param key the name of the session variable to retrieve
   * @param <T> the type of the value of the session variable being retrieved
   * @return the value of the session variable stored
   */
  @SuppressWarnings("unchecked")
  public <T> T get(final @NotNull Key<T> key) {
    return (T) map.get(key);
  }

  /**
   * A class representing the key for a session.
   *
   * @param <T> specifies the type of the stored value
   */
  public static final class Key<T> {

  }

  /**
   * Builder for Session.
   */
  public static class Builder {

    /**
     * A map of all session key and value.
     */
    private final Map<Key<?>, Object> map = new HashMap<>();

    /**
     * Adds a session variable into store.
     *
     * @param key   an unique name of the session variable
     * @param value the value of the session variable
     * @param <T>   the type of the value of the session variable
     * @return an instance of Builder
     */
    public final <T> Builder put(final @NotNull Key<T> key, final @Nullable T value) {
      map.put(key, value);
      return this;
    }

    /**
     * Create a new instance of session.
     *
     * @return A new instance of session.
     */
    public final Session build() {
      return new Session(this);
    }

  }

}
