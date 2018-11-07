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

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Maksim Tkachenko
 * @author Truong Quoc Tuan
 * @author Ween Jiann Lee
 */
public class HttpFetcherRequest implements Request, Unwrappable {

  /**
   * An instance of underlying request.
   */
  private final Request innerRequest;

  /**
   * The headers to append to global headers.
   */
  private final Map<String, String> headers;

  /**
   * The proxy to be used for this request.
   */
  private final HttpHost proxy;

  /**
   * Diagnostics for current request.
   */
  private final Diagnostics diagnostics;

  /**
   * Constructs an instance of http fetcher request.
   *
   * @param innerRequest An instance of underlying request
   */
  public HttpFetcherRequest(final Request innerRequest) {
    this(innerRequest, new HashMap<>(innerRequest.getHeaders()), innerRequest.getProxy());
  }

  /**
   * Constructs an instance of http fetcher request.
   *
   * @param innerRequest An instance of underlying request
   * @param headers      Headers to append to global headers
   * @param proxy        Proxy to be used for this request
   */
  private HttpFetcherRequest(final Request innerRequest, final Map<String, String> headers, final HttpHost proxy) {
    this.innerRequest = innerRequest;
    this.headers = headers;
    this.proxy = proxy;
    this.diagnostics = new Diagnostics();
  }

  /**
   * Prepend headers to the current headers.
   *
   * @param preHeaders Headers to be prepended
   * @return A new instance of http fetcher request
   */
  public final HttpFetcherRequest prependHeaders(final Map<String, String> preHeaders) {
    final Map<String, String> newHeaders = new HashMap<>(headers);
    preHeaders.forEach(newHeaders::putIfAbsent);
    return new HttpFetcherRequest(innerRequest, newHeaders, proxy);
  }

  @Override
  public final Method getMethod() {
    return innerRequest.getMethod();
  }

  @Override
  public final String getBody() {
    return innerRequest.getBody();
  }

  @Override
  public final String getUrl() {
    return innerRequest.getUrl();
  }

  @Override
  public final Map<String, String> getHeaders() {
    return Collections.unmodifiableMap(headers);
  }

  @Override
  public final HttpHost getProxy() {
    return proxy;
  }

  /**
   * Sets proxy to be used for this request.
   *
   * @param proxy Proxy to be used for this request
   * @return A new instance of http fetcher request
   */
  public final HttpFetcherRequest setProxy(final HttpHost proxy) {
    return new HttpFetcherRequest(innerRequest, headers, proxy);
  }

  @Override
  public final SleepScheduler getSleepScheduler() {
    return innerRequest.getSleepScheduler();
  }

  @Override
  public final Request getInner() {
    return innerRequest;
  }

  /**
   * Get diagnostic information for this request.
   *
   * @return A instance of diagnostics
   */
  public final Diagnostics getDiagnostics() {
    return diagnostics;
  }

  /**
   * This class contains the diagnostic information for this
   * request.
   */
  public static class Diagnostics {

    /**
     * Start time of the request.
     */
    private Long start;

    /**
     * Acknowledge time of the request.
     */
    private Long acknowledge;

    /**
     * Complete time of the request.
     */
    private Long complete;

    /**
     * Size of the response in bytes.
     */
    private Integer size;

    /**
     * Constructs an instance of diagnostics.
     */
    private Diagnostics() {
    }

    /**
     * Set the start time to current nano time.
     */
    public void setStart() {
      this.start = System.nanoTime();
    }

    /**
     * Set the acknowledge time to current nano time.
     */
    public void setAcknowledge() {
      this.acknowledge = System.nanoTime();
    }

    /**
     * Set the complete time to current nano time.
     */
    public void setComplete() {
      this.complete = System.nanoTime();
    }

    /**
     * Set the size of the response in bytes.
     *
     * @param size Size of the response.
     */
    public void setSize(int size) {
      this.size = size;
    }

    /**
     * Get the start time of the request. Returns null
     * if the start time has not been set.
     *
     * @return Time started
     */
    @Nullable
    public Long getStart() {
      return start;
    }

    /**
     * Get the acknowledge time of the request. Returns null
     * if the acknowledge time has not been set.
     *
     * @return Time acknowledged
     */
    @Nullable
    public Long getAcknowledge() {
      return acknowledge;
    }

    /**
     * Get the complete time of the request. Returns null
     * if the complete time has not been set.
     *
     * @return Time completed
     */
    @Nullable
    public Long getComplete() {
      return complete;
    }

    /**
     * Get the size the response. Returns null
     * if the response size has not been set.
     *
     * @return Response size
     */
    @Nullable
    public Integer getSize() {
      return size;
    }

    /**
     * Get the latency between sending of the request and the first
     * response. Returns null if {@link #isAcknowledged} is false.
     *
     * @return Time acknowledged
     */
    @Nullable
    public Long getLatency() {
      if (isAcknowledged()) {
        return acknowledge - start;
      }
      return null;
    }

    /**
     * Get download speed in bytes per second. Returns null if
     * {@link #isCompleted} is false.
     *
     * @return Download speed
     */
    @Nullable
    public Double getSpeed() {
      if (isCompleted()) {
        return size / ((complete - acknowledge) / 1000000000.0);
      }
      return null;
    }

    /**
     * Check if request has been started.
     *
     * @return True if request is started
     */
    public boolean isStarted() {
      return (start != null);
    }

    /**
     * Check if request has been started and acknowledged.
     *
     * @return True if request is started and acknowledged
     */
    public boolean isAcknowledged() {
      return isStarted() && (acknowledge != null);
    }

    /**
     * Check if request has been started, acknowledged and completed.
     *
     * @return True if request is started, acknowledged and completed
     */
    public boolean isCompleted() {
      return isStarted() && isAcknowledged() && (complete != null);
    }
  }
}
