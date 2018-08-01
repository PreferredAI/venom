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

package ai.preferred.venom.fetcher;

import ai.preferred.venom.ProxyProvider;
import ai.preferred.venom.ValidatorRouter;
import ai.preferred.venom.request.HttpFetcherRequest;
import ai.preferred.venom.request.Request;
import ai.preferred.venom.response.Response;
import ai.preferred.venom.storage.FileManager;
import ai.preferred.venom.uagent.DefaultUserAgent;
import ai.preferred.venom.uagent.UserAgent;
import ai.preferred.venom.validator.EmptyContentValidator;
import ai.preferred.venom.validator.PipelineValidator;
import ai.preferred.venom.validator.StatusOkValidator;
import ai.preferred.venom.validator.Validator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.concurrent.BasicFuture;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

/**
 * This class holds the implementation to provide how items are fetched, to fetch the item,
 * to validate the item and to store it if specified.
 *
 * @author Maksim Tkachenko
 * @author Truong Quoc Tuan
 * @author Ween Jiann Lee
 */
public class AsyncFetcher implements Fetcher {

  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(AsyncFetcher.class);

  /**
   * An instance of empty callback.
   */
  private static final FutureCallback<Response> EMPTY_CALLBACK = new FutureCallback<Response>() {
    @Override
    public void completed(final Response result) {

    }

    @Override
    public void failed(final Exception ex) {

    }

    @Override
    public void cancelled() {

    }
  };

  /**
   * A list of callbacks to execute upon response.
   */
  @NotNull
  private final List<Callback> callbacks;

  /**
   * A list of headers to append to request.
   */
  @NotNull
  private final Map<String, String> headers;

  /**
   * The HTTP client used for requests.
   */
  @NotNull
  private final CloseableHttpAsyncClient httpClient;

  /**
   * The proxy provider for proxies.
   */
  @Nullable
  private final ProxyProvider proxyProvider;

  /**
   * A list of status code to stop retry.
   */
  @NotNull
  private final Set<Integer> stopCodes;

  /**
   * The user agent used for requests.
   */
  @NotNull
  private final UserAgent userAgent;

  /**
   * The validator used.
   */
  @NotNull
  private final Validator validator;

  /**
   * The validator router used.
   */
  @Nullable
  private final ValidatorRouter router;

  /**
   * The timeout in milliseconds used when requesting a connection.
   */
  private final int connectionRequestTimeout;

  /**
   * Determines whether compression is allowed.
   */
  private final boolean compressed;

  /**
   * Constructs an instance of async fetcher.
   *
   * @param builder An instance of builder
   */
  private AsyncFetcher(final Builder builder) {
    final ImmutableList.Builder<Callback> callbackListBuilder = new ImmutableList.Builder<>();
    if (builder.fileManager != null) {
      callbackListBuilder.add(builder.fileManager.getCallback());
    }
    callbackListBuilder.addAll(builder.callbacks);
    callbacks = callbackListBuilder.build();
    headers = builder.headers;
    proxyProvider = builder.proxyProvider;
    stopCodes = builder.stopCodes;
    userAgent = builder.userAgent;
    validator = builder.validator;
    router = builder.router;
    connectionRequestTimeout = builder.connectionRequestTimeout;
    compressed = builder.compressed;

    final IOReactorConfig reactorConfig = IOReactorConfig.custom()
        .setIoThreadCount(builder.numIoThreads)
        .setSoKeepAlive(true)
        .setTcpNoDelay(true)
        .setConnectTimeout(builder.connectTimeout)
        .setSoTimeout(builder.socketTimeout)
        .build();

    final HttpAsyncClientBuilder clientBuilder = HttpAsyncClientBuilder.create()
        .setDefaultIOReactorConfig(reactorConfig)
        .setThreadFactory(builder.threadFactory);

    if (builder.compressed) {
      clientBuilder.addInterceptorLast(new RequestAcceptEncoding());
    }

    httpClient = clientBuilder.build();
  }

  /**
   * Create an instance of async fetcher with default options.
   *
   * @return A new instance of async fetcher
   */
  public static AsyncFetcher buildDefault() {
    return builder().build();
  }

  /**
   * Create an instance of builder.
   *
   * @return A new instance of builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Check if request is an instance of http fetcher request and return it
   * if true, otherwise wrap it with http fetcher request and return that.
   *
   * @param request An instance of request
   * @return An instance of http fetcher request
   */
  private HttpFetcherRequest normalizeRequest(final Request request) {
    if (request instanceof HttpFetcherRequest) {
      return (HttpFetcherRequest) request;
    }
    return new HttpFetcherRequest(request);
  }

  /**
   * Prepare fetcher request by prepending headers and set appropriate proxy.
   *
   * @param request An instance of request
   * @return An instance of http fetcher request
   */
  private HttpFetcherRequest prepareFetcherRequest(final Request request) {
    HttpFetcherRequest httpFetcherRequest = normalizeRequest(request);

    if (!headers.isEmpty()) {
      httpFetcherRequest = httpFetcherRequest.prependHeaders(headers);
    }

    if (proxyProvider != null && httpFetcherRequest.getInner().getProxy() == null) {
      httpFetcherRequest = httpFetcherRequest.setProxy(proxyProvider.get(request));
    }

    return httpFetcherRequest;
  }

  /**
   * Create an instance of request builder.
   *
   * @param request An instance of request
   * @return An instance of request builder
   */
  private RequestBuilder createRequestBuilder(final Request request) {
    switch (request.getMethod()) {
      case GET:
        return RequestBuilder.get();
      case POST:
        return RequestBuilder.post();
      case HEAD:
        return RequestBuilder.head();
      case PUT:
        return RequestBuilder.put();
      case DELETE:
        return RequestBuilder.delete();
      case OPTIONS:
        return RequestBuilder.options();
      default:
        throw new RuntimeException("Request method is not defined");
    }
  }

  /**
   * Prepare http uri request to be used with http async client.
   *
   * @param request An instance of request
   * @return An instance of http uri request
   */
  private HttpUriRequest prepareHttpRequest(final HttpFetcherRequest request) {
    final RequestConfig config = RequestConfig.custom()
        .setConnectionRequestTimeout(connectionRequestTimeout)
        .setProxy(request.getProxy())
        .build();

    final RequestBuilder requestBuilder = createRequestBuilder(request)
        .addHeader("User-Agent", userAgent.get())
        .setUri(request.getUrl())
        .setConfig(config);

    request.getHeaders().forEach(requestBuilder::setHeader);

    if (request.getBody() != null) {
      requestBuilder.setEntity(new ByteArrayEntity(request.getBody().getBytes()));
    }

    return requestBuilder.build();
  }

  /**
   * Append routed validator if present for this request.
   *
   * @param routedValidator An instance of routed validator
   * @return An instance of validator
   */
  private Validator prepareValidator(final Validator routedValidator) {
    if (routedValidator == null) {
      return validator;
    }

    return new PipelineValidator(validator, routedValidator);
  }

  /**
   * Copied from {@link CloseableHttpAsyncClient}.
   *
   * @param request request
   * @return target
   * @throws ClientProtocolException Non-valid protocol
   */
  private HttpHost determineTarget(final HttpUriRequest request) throws ClientProtocolException {
    // A null target may be acceptable if there is a default target.
    // Otherwise, the null target is detected in the director.
    HttpHost target = null;

    final URI requestURI = request.getURI();
    if (requestURI.isAbsolute()) {
      target = URIUtils.extractHost(requestURI);
      if (target == null) {
        throw new ClientProtocolException(
            "URI does not specify a valid host name: " + requestURI);
      }
    }
    return target;
  }

  @Override
  public Future<Response> fetch(final Request request) {
    return fetch(request, EMPTY_CALLBACK);
  }

  @Override
  public Future<Response> fetch(final Request request, final FutureCallback<Response> callback) {
    final HttpFetcherRequest httpFetcherRequest = prepareFetcherRequest(request);

    final FutureCallback<Response> futureCallback = new FutureCallback<Response>() {
      @Override
      public void completed(final Response response) {
        LOGGER.debug("Executing completion callback on {}.", request.getUrl());
        callbacks.forEach(callback -> callback.completed(httpFetcherRequest, response));
        callback.completed(response);
      }

      @Override
      public void failed(final Exception ex) {
        LOGGER.debug("Executing failed callback on {}.", request.getUrl(), ex);
        callbacks.forEach(callback -> callback.failed(httpFetcherRequest, ex));
        callback.failed(ex);
      }

      @Override
      public void cancelled() {
        LOGGER.debug("Executing cancelled callback on {}.", request.getUrl());
        callbacks.forEach(callback -> callback.cancelled(httpFetcherRequest));
        callback.cancelled();
      }
    };

    final HttpUriRequest httpReq = prepareHttpRequest(httpFetcherRequest);
    final HttpHost target;
    try {
      target = determineTarget(httpReq);
    } catch (final ClientProtocolException ex) {
      final BasicFuture<Response> future = new BasicFuture<>(futureCallback);
      future.failed(ex);
      return future;
    }

    LOGGER.debug("Fetching URL: {}", request.getUrl());

    final Validator routedValidator;
    if (router != null) {
      routedValidator = router.getValidator(request);
    } else {
      routedValidator = null;
    }

    return httpClient.execute(
        HttpAsyncMethods.create(target, httpReq),
        new AsyncResponseConsumer(
            prepareValidator(routedValidator),
            stopCodes,
            compressed,
            httpFetcherRequest
        ),
        HttpClientContext.create(),
        futureCallback
    );
  }

  @Override
  public void start() {
    httpClient.start();
  }

  @Override
  public void close() throws Exception {
    LOGGER.debug("Initialising fetcher shutdown...");
    httpClient.close();
    LOGGER.debug("Fetcher shutdown completed.");
  }

  /**
   * A builder for async fetcher class.
   */
  public static class Builder {

    /**
     * A list of callbacks to execute upon response.
     */
    private final List<Callback> callbacks;

    /**
     * The file manager used to store raw responses.
     */
    private FileManager fileManager;

    /**
     * A list of headers to append to request.
     */
    private Map<String, String> headers;

    /**
     * The maximum number of I/O threads allowed.
     */
    private int numIoThreads;

    /**
     * The proxy provider for proxies.
     */
    private ProxyProvider proxyProvider;

    /**
     * A list of status code to stop retry.
     */
    private Set<Integer> stopCodes;

    /**
     * The threadFactory used for I/O dispatcher.
     */
    private ThreadFactory threadFactory;

    /**
     * The user agent used for requests.
     */
    private UserAgent userAgent;

    /**
     * The validator used.
     */
    private Validator validator;

    /**
     * The validator router used.
     */
    private ValidatorRouter router;

    /**
     * The timeout in milliseconds used when requesting a connection.
     */
    private int connectionRequestTimeout;

    /**
     * The timeout in milliseconds until a connection is established.
     */
    private int connectTimeout;

    /**
     * The socket timeout ({@code SO_TIMEOUT}) in milliseconds.
     */
    private int socketTimeout;

    /**
     * Determines whether compression is allowed.
     */
    private boolean compressed;

    /**
     * Construct an instance of builder.
     */
    private Builder() {
      callbacks = new ArrayList<>();
      fileManager = null;
      headers = Collections.emptyMap();
      numIoThreads = Runtime.getRuntime().availableProcessors();
      proxyProvider = null;
      stopCodes = Collections.emptySet();
      threadFactory = new ThreadFactoryBuilder().setNameFormat("I/O Dispatcher %d").build();
      userAgent = new DefaultUserAgent();
      validator = new PipelineValidator(
          StatusOkValidator.INSTANCE,
          EmptyContentValidator.INSTANCE
      );
      router = request -> Validator.ALWAYS_VALID;
      connectionRequestTimeout = -1;
      connectTimeout = -1;
      socketTimeout = -1;
      compressed = true;
    }

    /**
     * Register any callbacks that will be called when a page has been fetched.
     * <p>
     * Please note that blocking callbacks will significantly reduce the rate
     * at which request are processed. Please implement your own executors on
     * I/O blocking callbacks.
     * </p>
     *
     * @param callback A set of FetcherCallback.
     * @return this
     */
    public Builder register(final @NotNull Callback callback) {
      this.callbacks.add(callback);
      return this;
    }

    /**
     * Sets the FileManager to be used. Defaults to none.
     * <p>
     * If fileManager is set, all items fetched will be saved to storage.
     * </p>
     *
     * @param fileManager file manager to be used.
     * @return this
     */
    public Builder fileManager(final @NotNull FileManager fileManager) {
      this.fileManager = fileManager;
      return this;
    }

    /**
     * Sets the headers to be used when fetching items. Defaults to none.
     *
     * @param headers a map to headers to be used.
     * @return this
     */
    public Builder headers(final @NotNull Map<String, String> headers) {
      this.headers = headers;
      return this;
    }

    /**
     * Number of httpclient dispatcher threads.
     *
     * @param numIoThreads number of threads.
     * @return this
     */
    public Builder numIoThreads(final int numIoThreads) {
      this.numIoThreads = numIoThreads;
      return this;
    }

    /**
     * Sets the ProxyProvider to be used. Defaults to none.
     *
     * @param proxyProvider proxy provider to be used
     * @return this
     */
    public Builder proxyProvider(final @NotNull ProxyProvider proxyProvider) {
      this.proxyProvider = proxyProvider;
      return this;
    }

    /**
     * Set a list of stop code that will interrupt crawling.
     *
     * @param codes A list of stop codes
     * @return this
     */
    public Builder stopCodes(final int... codes) {
      ImmutableSet.Builder<Integer> builder = new ImmutableSet.Builder<>();
      for (int code : codes) {
        builder.add(code);
      }
      stopCodes = builder.build();
      return this;
    }

    /**
     * Set the thread factory that creates the httpclient dispatcher
     * threads.
     *
     * @param threadFactory an instance of ThreadFactory.
     * @return this
     */
    public Builder threadFactory(final @NotNull ThreadFactory threadFactory) {
      this.threadFactory = threadFactory;
      return this;
    }

    /**
     * Sets the UserAgent to be used, if not set, default will be chosen.
     *
     * @param userAgent user agent generator to be used.
     * @return this
     */
    public Builder userAgent(final @NotNull UserAgent userAgent) {
      this.userAgent = userAgent;
      return this;
    }

    /**
     * Sets the Validator to be used. Defaults to none.
     * <p>
     * This will validate the fetched page and retry if page is not
     * consistent with the specification set by the validator.
     * </p>
     *
     * @param validator validator to be used.
     * @return this
     */
    public Builder validator(final @NotNull Validator validator) {
      this.validator = validator;
      return this;
    }

    /**
     * Sets ValidatorRouter to be used. Defaults to none.
     * Validator rules set in validator will always be used.
     *
     * @param router router validator router to be used.
     * @return this
     */
    public Builder router(final @NotNull ValidatorRouter router) {
      this.router = router;
      return this;
    }

    /**
     * The timeout in milliseconds used when requesting a connection
     * from the connection manager. A timeout value of zero is interpreted
     * as an infinite timeout.
     *
     * @param connectionRequestTimeout timeout.
     * @return this
     */
    public Builder connectionRequestTimeout(final int connectionRequestTimeout) {
      this.connectionRequestTimeout = connectionRequestTimeout;
      return this;
    }

    /**
     * Determines the timeout in milliseconds until a connection is established.
     * A timeout value of zero is interpreted as an infinite timeout.
     *
     * @param connectTimeout timeout.
     * @return this
     */
    public Builder connectTimeout(final int connectTimeout) {
      this.connectTimeout = connectTimeout;
      return this;
    }

    /**
     * Defines the socket timeout ({@code SO_TIMEOUT}) in milliseconds,
     * which is the timeout for waiting for data  or, put differently,
     * a maximum period inactivity between two consecutive data packets).
     *
     * @param socketTimeout timeout.
     * @return this
     */
    public Builder socketTimeout(final int socketTimeout) {
      this.socketTimeout = socketTimeout;
      return this;
    }

    /**
     * Set whether to request for compress pages and to decompress pages
     * after it is fetched. Defaults to true.
     *
     * @param compressed should request for compress pages
     * @return this
     */
    public Builder compressed(final boolean compressed) {
      this.compressed = compressed;
      return this;
    }

    /**
     * Builds the fetcher with the options specified.
     *
     * @return an instance of Fetcher.
     */
    public AsyncFetcher build() {
      return new AsyncFetcher(this);
    }

  }

}
