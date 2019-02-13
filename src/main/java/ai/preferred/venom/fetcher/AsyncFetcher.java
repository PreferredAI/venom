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
import org.apache.http.client.RedirectStrategy;
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
import javax.net.ssl.SSLContext;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

/**
 * This class holds the implementation to provide how items are fetched from the web,
 * to validate the item and to store it if specified.
 *
 * @author Maksim Tkachenko
 * @author Truong Quoc Tuan
 * @author Ween Jiann Lee
 */
public final class AsyncFetcher implements Fetcher {

  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(AsyncFetcher.class);

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
        .setConnectTimeout(builder.connectTimeout)
        .setSoTimeout(builder.socketTimeout)
        .build();

    final HttpAsyncClientBuilder clientBuilder = HttpAsyncClientBuilder.create()
        .setDefaultIOReactorConfig(reactorConfig)
        .setThreadFactory(builder.threadFactory)
        .setMaxConnPerRoute(builder.maxRouteConnections)
        .setMaxConnTotal(builder.maxConnections)
        .setSSLContext(builder.sslContext)
        .setRedirectStrategy(builder.redirectStrategy);

    if (builder.maxConnections < builder.maxRouteConnections) {
      clientBuilder.setMaxConnTotal(builder.maxRouteConnections);
      LOGGER.info("Maximum total connections will be set to {}, to match maximum route connection.",
          builder.maxRouteConnections);
    }

    if (builder.disableCookies) {
      clientBuilder.disableCookieManagement();
    }

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
    return fetch(request, Callback.EMPTY_CALLBACK);
  }

  @Override
  public Future<Response> fetch(final Request request, final Callback callback) {
    final HttpFetcherRequest httpFetcherRequest = prepareFetcherRequest(request);

    final FutureCallback<Response> futureCallback = new FutureCallback<Response>() {
      @Override
      public void completed(final Response response) {
        LOGGER.debug("Executing completion callback on {}.", request.getUrl());
        callbacks.forEach(callback -> callback.completed(httpFetcherRequest, response));
        callback.completed(httpFetcherRequest, response);
      }

      @Override
      public void failed(final Exception ex) {
        LOGGER.debug("Executing failed callback on {}.", request.getUrl(), ex);
        callbacks.forEach(callback -> callback.failed(httpFetcherRequest, ex));
        callback.failed(httpFetcherRequest, ex);
      }

      @Override
      public void cancelled() {
        LOGGER.debug("Executing cancelled callback on {}.", request.getUrl());
        callbacks.forEach(callback -> callback.cancelled(httpFetcherRequest));
        callback.cancelled(httpFetcherRequest);
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

    if (!httpClient.isRunning()) {
      final BasicFuture<Response> future = new BasicFuture<>(futureCallback);
      future.cancel(true);
      return future;
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
  public void close() throws IOException {
    LOGGER.debug("Shutting down the fetcher...");
    httpClient.close();
    LOGGER.debug("The fetcher shutdown completed.");
  }

  /**
   * A builder for async fetcher class.
   */
  public static final class Builder {

    /**
     * A list of callbacks to execute upon response.
     */
    private final List<Callback> callbacks;

    /**
     * Determines whether cookie storage is allowed.
     */
    private boolean disableCookies;

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
     * The maximum number of connections allowed.
     */
    private int maxConnections;

    /**
     * The maximum number of connections allowed per route.
     */
    private int maxRouteConnections;

    /**
     * The proxy provider for proxies.
     */
    private ProxyProvider proxyProvider;

    /**
     * The SSL context for a response.
     */
    private SSLContext sslContext;

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
     * The redirection strategy for a response.
     */
    private RedirectStrategy redirectStrategy;

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
      disableCookies = false;
      fileManager = null;
      headers = Collections.emptyMap();
      maxConnections = 16;
      maxRouteConnections = 8;
      numIoThreads = Runtime.getRuntime().availableProcessors();
      proxyProvider = null;
      stopCodes = Collections.emptySet();
      threadFactory = new ThreadFactoryBuilder().setNameFormat("I/O Dispatcher %d").build();
      userAgent = new DefaultUserAgent();
      validator = new PipelineValidator(
          StatusOkValidator.INSTANCE,
          EmptyContentValidator.INSTANCE
      );
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
     * Disables cookie storage.
     *
     * @return this
     */
    public Builder disableCookies() {
      this.disableCookies = true;
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
    public Builder setFileManager(final @NotNull FileManager fileManager) {
      this.fileManager = fileManager;
      return this;
    }

    /**
     * Sets the headers to be used when fetching items. Defaults to none.
     *
     * @param headers a map to headers to be used.
     * @return this
     */
    public Builder setHeaders(final @NotNull Map<String, String> headers) {
      this.headers = headers;
      return this;
    }

    /**
     * Number of httpclient dispatcher threads.
     *
     * @param numIoThreads number of threads.
     * @return this
     */
    public Builder setNumIoThreads(final int numIoThreads) {
      this.numIoThreads = numIoThreads;
      return this;
    }

    /**
     * Sets the maximum allowable connections at an instance.
     *
     * @param maxConnections the max allowable connections.
     * @return this
     */
    public Builder setMaxConnections(final int maxConnections) {
      this.maxConnections = maxConnections;
      return this;
    }

    /**
     * Sets the maximum allowable connections at an instance for
     * a particular route (host).
     *
     * @param maxRouteConnections the max allowable connections per route.
     * @return this
     */
    public Builder setMaxRouteConnections(final int maxRouteConnections) {
      this.maxRouteConnections = maxRouteConnections;
      return this;
    }

    /**
     * Sets the ProxyProvider to be used. Defaults to none.
     *
     * @param proxyProvider proxy provider to be used.
     * @return this
     */
    public Builder setProxyProvider(final @NotNull ProxyProvider proxyProvider) {
      this.proxyProvider = proxyProvider;
      return this;
    }

    /**
     * Sets the ssl context for an encrypted response.
     *
     * @param sslContext SSLContext to be used.
     * @return this
     */
    public Builder setSslContext(final SSLContext sslContext) {
      this.sslContext = sslContext;
      return this;
    }

    /**
     * Set a list of stop code that will interrupt crawling.
     *
     * @param codes A list of stop codes.
     * @return this
     */
    public Builder setStopCodes(final int... codes) {
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
    public Builder setThreadFactory(final @NotNull ThreadFactory threadFactory) {
      this.threadFactory = threadFactory;
      return this;
    }

    /**
     * Sets the UserAgent to be used, if not set, default will be chosen.
     *
     * @param userAgent user agent generator to be used.
     * @return this
     */
    public Builder setUserAgent(final @NotNull UserAgent userAgent) {
      this.userAgent = userAgent;
      return this;
    }

    /**
     * Sets the Validator to be used. Defaults to StatusOkValidator and
     * EmptyContentValidator.
     * <p>
     * This will validate the fetched page and retry if page is not
     * consistent with the specification set by the validator.
     * </p>
     *
     * @param validator validator to be used.
     * @return this
     */
    public Builder setValidator(final @NotNull Validator validator) {
      this.validator = validator;
      return this;
    }

    /**
     * Sets the multiple validators to be used. Defaults to StatusOkValidator
     * and EmptyContentValidator.
     * <p>
     * This will validate the fetched page and retry if page is not
     * consistent with the specification set by the validator.
     * </p>
     *
     * @param validators validator to be used.
     * @return this
     */
    public Builder setValidator(final @NotNull Validator... validators) {
      this.validator = new PipelineValidator(validators);
      return this;
    }

    /**
     * Sets the redirection strategy for a response received by the fetcher.
     *
     * @param redirectStrategy redirection strategy to be used.
     * @return this
     */
    public Builder setRedirectStrategy(final RedirectStrategy redirectStrategy) {
      this.redirectStrategy = redirectStrategy;
      return this;
    }

    /**
     * Sets ValidatorRouter to be used. Defaults to none.
     * Validator rules set in validator will always be used.
     *
     * @param router router validator setValidatorRouter to be used.
     * @return this
     */
    public Builder setValidatorRouter(final @NotNull ValidatorRouter router) {
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
    public Builder setConnectionRequestTimeout(final int connectionRequestTimeout) {
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
    public Builder setConnectTimeout(final int connectTimeout) {
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
    public Builder setSocketTimeout(final int socketTimeout) {
      this.socketTimeout = socketTimeout;
      return this;
    }

    /**
     * Disables request for compress pages and to decompress pages
     * after it is fetched. Defaults to true.
     *
     * @return this
     */
    public Builder disableCompression() {
      this.compressed = false;
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
