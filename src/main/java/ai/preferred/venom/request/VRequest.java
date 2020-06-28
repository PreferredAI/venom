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

package ai.preferred.venom.request;

import ai.preferred.venom.SleepScheduler;
import org.apache.http.HttpHost;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of HTTP request.
 *
 * @author Maksim Tkachenko
 * @author Truong Quoc Tuan
 * @author Ween Jiann Lee
 */
public class VRequest implements Request {

  /**
   * The method of this request.
   */
  private final Method method;
  /**
   * The url for this request.
   */
  private final String url;
  /**
   * The headers to append to global headers.
   */
  private final Map<String, String> headers;
  /**
   * The body of this request.
   */
  private final String body;
  /**
   * The proxy to be used for this request.
   */
  private final HttpHost proxy;
  /**
   * The sleep scheduler to be used for this request.
   */
  private final SleepScheduler sleepScheduler;

  /**
   * Constructs an instance of venom request.
   *
   * @param url The url for this request.
   */
  public VRequest(final String url) {
    this(url, Collections.emptyMap());
  }

  /**
   * Constructs an instance of venom request.
   *
   * @param url     The url for this request
   * @param headers The headers to append for this request
   */
  public VRequest(final String url, final Map<String, String> headers) {
    this(Method.GET, url, new HashMap<>(headers), null, null, null);
  }

  /**
   * Constructs an instance of venom request.
   *
   * @param builder An instance of builder
   */
  protected VRequest(final Builder<?> builder) {
    this(builder.method == null ? Method.GET : builder.method,
        builder.url,
        new HashMap<>(builder.headers),
        builder.body,
        builder.scheduler,
        builder.proxy
    );
  }

  /**
   * Constructs an instance of venom request.
   *
   * @param method         The method for this request
   * @param url            The url for this request
   * @param headers        The headers to append for this request
   * @param body           The body for this request
   * @param sleepScheduler The sleep scheduler to use
   * @param proxy          The proxy to use
   */
  private VRequest(final Method method, final String url, final Map<String, String> headers, final String body,
                   final SleepScheduler sleepScheduler, final HttpHost proxy) {
    this.method = method;
    this.url = url;
    this.headers = headers;
    this.body = body;
    this.sleepScheduler = sleepScheduler;
    this.proxy = proxy;
  }

  /**
   * Create a new instance of builder with a method and url.
   *
   * @param method Request method
   * @param url    Request url
   * @return A new instance of builder
   */
  public static Builder<?> build(final Method method, final String url) {
    return new Builder<>(method, url);
  }

  @Override
  public final Method getMethod() {
    return method;
  }

  @Override
  public final String getBody() {
    return body;
  }

  @Override
  public final String getUrl() {
    return url;
  }

  @Override
  public final Map<String, String> getHeaders() {
    return Collections.unmodifiableMap(headers);
  }

  @Override
  public final HttpHost getProxy() {
    return proxy;
  }

  @Override
  public final SleepScheduler getSleepScheduler() {
    return sleepScheduler;
  }

  /**
   * A builder for VRequest class.
   *
   * @param <T> An class that extends builder
   */
  public static class Builder<T extends Builder<T>> {

    /**
     * The headers to append to global headers.
     */
    private final Map<String, String> headers = new HashMap<>();
    /**
     * The method of this request.
     */
    private final Method method;
    /**
     * The body of this request.
     */
    private String body;
    /**
     * The url for this request.
     */
    private String url;
    /**
     * The proxy to be used for this request.
     */
    private HttpHost proxy;
    /**
     * The sleep scheduler to be used for this request.
     */
    private SleepScheduler scheduler;

    /**
     * Constructs an instance of builder.
     *
     * @param method The method for this request
     * @param url    The url for this request
     */
    protected Builder(final Method method, final String url) {
      this.method = method;
      this.url = url;
    }

    /**
     * Creates a new instance of builder with method type get.
     *
     * @param url url to fetch.
     * @return an instance of builder.
     */
    public static Builder<?> get(final String url) {
      return new Builder<>(Method.GET, url);
    }

    /**
     * Creates a new instance of builder with method type post.
     *
     * @param url url to fetch.
     * @return an instance of builder.
     */
    public static Builder<?> post(final String url) {
      return new Builder<>(Method.POST, url);
    }

    /**
     * Creates a new instance of builder with method type head.
     *
     * @param url url to fetch.
     * @return an instance of builder.
     */
    public static Builder<?> head(final String url) {
      return new Builder<>(Method.HEAD, url);
    }

    /**
     * Creates a new instance of builder with method type put.
     *
     * @param url url to fetch.
     * @return an instance of builder.
     */
    public static Builder<?> put(final String url) {
      return new Builder<>(Method.PUT, url);
    }

    /**
     * Creates a new instance of builder with method type delete.
     *
     * @param url url to fetch.
     * @return an instance of builder.
     */
    public static Builder<?> delete(final String url) {
      return new Builder<>(Method.DELETE, url);
    }

    /**
     * Creates a new instance of builder with method type options.
     *
     * @param url url to fetch.
     * @return an instance of builder.
     */
    public static Builder<?> options(final String url) {
      return new Builder<>(Method.OPTIONS, url);
    }

    /**
     * Sets the request body to be used.
     *
     * @param body request body
     * @return this
     */
    @SuppressWarnings("unchecked")
    public final T setBody(final String body) {
      this.body = body;
      return (T) this;
    }

    /**
     * Sets the sleep scheduler to be used, this will override the
     * sleep scheduler defined in Crawler for this request. Defaults
     * to none.
     *
     * @param scheduler sleep scheduler to be used.
     * @return this
     */
    @SuppressWarnings("unchecked")
    public final T setSleepScheduler(final SleepScheduler scheduler) {
      this.scheduler = scheduler;
      return (T) this;
    }

    /**
     * Sets the proxy to be used, this will override the
     * proxy selected in Fetcher for this request. Defaults
     * to none.
     *
     * @param proxy proxy to be used.
     * @return this
     */
    @SuppressWarnings("unchecked")
    public final T setProxy(final HttpHost proxy) {
      this.proxy = proxy;
      return (T) this;
    }

    /**
     * Remove a header from this request.
     *
     * @param name The key of the header to remove
     * @return this
     */
    @SuppressWarnings("unchecked")
    public final T removeHeader(final String name) {
      headers.remove(name);
      return (T) this;
    }

    /**
     * Remove all headers from this request.
     *
     * @return this
     */
    @SuppressWarnings("unchecked")
    public final T removeHeaders() {
      headers.clear();
      return (T) this;
    }

    /**
     * Add headers to be used
     * <p>
     * This will merge with headers set in Crawler class. If
     * a same key is found, this will override that header in
     * Crawler class.
     * </p>
     *
     * @param headers request headers
     * @return this
     */
    @SuppressWarnings("unchecked")
    public final T addHeaders(final Map<String, String> headers) {
      this.headers.putAll(headers);
      return (T) this;
    }

    /**
     * Adds header to be used
     * <p>
     * This will merge with headers set in Crawler class. If
     * a same key is found, this will override that header in
     * Crawler class.
     * </p>
     *
     * @param name  name/key of the header
     * @param value value of the header
     * @return this
     */
    @SuppressWarnings("unchecked")
    public final T addHeader(final String name, final String value) {
      headers.put(name, value);
      return (T) this;
    }

    /**
     * Sets the url to be fetched.
     *
     * @param url url to fetch.
     * @return this
     */
    @SuppressWarnings("unchecked")
    public final T setUrl(final String url) {
      this.url = url;
      return (T) this;
    }

    /**
     * Builds the request with the options specified.
     *
     * @return an instance of Request.
     */
    public VRequest build() {
      return new VRequest(this);
    }

  }

}
