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

  public static Builder build(Method method, String url) {
    return new Builder(method, url);
  }

  /**
   * A builder for VRequest class
   */
  public static class Builder<T extends Builder<T>> {

    /**
     * Creates a new instance of builder with method type get.
     *
     * @param url url to fetch.
     * @return an instance of builder.
     */
    public static Builder get(String url) {
      return new Builder(Method.GET, url);
    }

    /**
     * Creates a new instance of builder with method type post.
     *
     * @param url url to fetch.
     * @return an instance of builder.
     */
    public static Builder post(String url) {
      return new Builder(Method.POST, url);
    }

    /**
     * Creates a new instance of builder with method type head.
     *
     * @param url url to fetch.
     * @return an instance of builder.
     */
    public static Builder head(String url) {
      return new Builder(Method.HEAD, url);
    }

    /**
     * Creates a new instance of builder with method type put.
     *
     * @param url url to fetch.
     * @return an instance of builder.
     */
    public static Builder put(String url) {
      return new Builder(Method.PUT, url);
    }

    /**
     * Creates a new instance of builder with method type delete.
     *
     * @param url url to fetch.
     * @return an instance of builder.
     */
    public static Builder delete(String url) {
      return new Builder(Method.DELETE, url);
    }

    /**
     * Creates a new instance of builder with method type options.
     *
     * @param url url to fetch.
     * @return an instance of builder.
     */
    public static Builder options(String url) {
      return new Builder(Method.OPTIONS, url);
    }

    private final Map<String, String> headers = new HashMap<>();

    private final Method method;

    private String body;

    private String url;

    private HttpHost proxy;

    private SleepScheduler scheduler;

    protected Builder(Method method, String url) {
      this.method = method;
      this.url = url;
    }

    /**
     * Sets the request body to be used.
     *
     * @param body request body
     * @return this.
     */
    @SuppressWarnings("unchecked")
    public T setBody(String body) {
      this.body = body;
      return (T) this;
    }

    /**
     * Sets the sleep scheduler to be used, this will override the
     * sleep scheduler defined in Crawler for this request. Defaults
     * to none.
     *
     * @param scheduler sleep scheduler to be used.
     * @return this.
     */
    @SuppressWarnings("unchecked")
    public T setSleepScheduler(SleepScheduler scheduler) {
      this.scheduler = scheduler;
      return (T) this;
    }

    /**
     * Sets the proxy to be used, this will override the
     * proxy selected in Fetcher for this request. Defaults
     * to none.
     *
     * @param proxy proxy to be used.
     * @return this.
     */
    @SuppressWarnings("unchecked")
    public T setProxy(HttpHost proxy) {
      this.proxy = proxy;
      return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T removeHeader(String name) {
      headers.remove(name);
      return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T removeHeaders() {
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
     * @return this.
     */
    @SuppressWarnings("unchecked")
    public T addHeaders(Map<String, String> headers) {
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
     * @return this.
     */
    @SuppressWarnings("unchecked")
    public T addHeader(String name, String value) {
      headers.put(name, value);
      return (T) this;
    }

    /**
     * Sets the url to be fetched.
     *
     * @param url url to fetch.
     * @return this.
     */
    @SuppressWarnings("unchecked")
    public T setUrl(String url) {
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

  private final Method method;
  private final String url;
  private final Map<String, String> headers;
  private final String body;
  private final SleepScheduler sleepScheduler;
  private final HttpHost proxy;

  public VRequest(String url) {
    this(url, Collections.emptyMap());
  }

  public VRequest(String url, Map<String, String> headers) {
    this(Method.GET, url, new HashMap<>(headers), null, null, null);
  }

  protected VRequest(Builder<?> builder) {
    this(builder.method == null ? Method.GET : builder.method,
        builder.url,
        new HashMap<>(builder.headers),
        builder.body,
        builder.scheduler,
        builder.proxy
    );
  }

  private VRequest(Method method, String url, Map<String, String> headers, String body,
                   SleepScheduler sleepScheduler, HttpHost proxy) {
    this.method = method;
    this.url = url;
    this.headers = headers;
    this.body = body;
    this.sleepScheduler = sleepScheduler;
    this.proxy = proxy;
  }

  @Override
  public Method getMethod() {
    return method;
  }

  @Override
  public String getBody() {
    return body;
  }

  @Override
  public String getUrl() {
    return url;
  }

  @Override
  public Map<String, String> getHeaders() {
    return Collections.unmodifiableMap(headers);
  }

  @Override
  public HttpHost getProxy() {
    return proxy;
  }

  @Override
  public SleepScheduler getSleepScheduler() {
    return sleepScheduler;
  }

}
