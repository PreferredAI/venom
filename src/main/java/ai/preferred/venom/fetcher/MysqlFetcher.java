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

import ai.preferred.venom.request.MysqlFetcherRequest;
import ai.preferred.venom.request.Request;
import ai.preferred.venom.request.Unwrappable;
import ai.preferred.venom.response.Response;
import ai.preferred.venom.response.StorageResponse;
import ai.preferred.venom.storage.FileManager;
import ai.preferred.venom.storage.Record;
import ai.preferred.venom.storage.StorageException;
import ai.preferred.venom.utils.UrlUtil;
import ai.preferred.venom.validator.EmptyContentValidator;
import ai.preferred.venom.validator.PipelineValidator;
import ai.preferred.venom.validator.StatusOkValidator;
import ai.preferred.venom.validator.Validator;
import org.apache.http.ParseException;
import org.apache.http.concurrent.BasicFuture;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * This class holds the implementation to provide how items are fetched from a MySQL database,
 * to validate the item and to store it if specified.
 *
 * @author Ween Jiann Lee
 */
public final class MysqlFetcher implements Fetcher {

  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(MysqlFetcher.class);

  /**
   * Default content type of response if not given.
   */
  private static final ContentType DEFAULT_CONTENT_TYPE = ContentType.APPLICATION_OCTET_STREAM;

  /**
   * The file manager used to store raw responses.
   */
  private final FileManager fileManager;

  /**
   * The validator used.
   */
  private final Validator validator;

  /**
   * A list of headers to append to request.
   */
  private final Map<String, String> headers;

  /**
   * Constructs an instance of MySQL fetcher.
   *
   * @param builder An instance of builder
   */
  private MysqlFetcher(final Builder builder) {
    this.fileManager = builder.fileManager;
    this.validator = builder.validator;
    this.headers = builder.headers;
  }

  /**
   * Create an instance of builder.
   *
   * @param fileManager the file manager to use.
   * @return A new instance of builder
   */
  public static Builder builder(final FileManager fileManager) {
    return new Builder(fileManager);
  }

  /**
   * Check if request is an instance of MySQL fetcher request and return it
   * if true, otherwise wrap it with MySQL fetcher request and return that.
   *
   * @param request An instance of request
   * @return An instance of MySQL fetcher request
   */
  private MysqlFetcherRequest normalize(final Request request) {
    if (request instanceof MysqlFetcherRequest) {
      return (MysqlFetcherRequest) request;
    }
    return new MysqlFetcherRequest(request);
  }

  /**
   * Get content type from record, if not found return default.
   *
   * @param record an instance of record
   * @return an instance of content type
   */
  private ContentType getContentType(final Record record) {
    try {
      return ContentType.create(record.getMimeType(), record.getEncoding());
    } catch (ParseException e) {
      LOGGER.warn("Could not parse content type", e);
    } catch (UnsupportedCharsetException e) {
      LOGGER.warn("Charset is not available in this instance of the Java virtual machine", e);
    }
    return DEFAULT_CONTENT_TYPE;
  }

  @Override
  public void start() {

  }

  @Override
  public Future<Response> fetch(final Request request) {
    return fetch(request, Callback.EMPTY_CALLBACK);
  }

  @Override
  public Future<Response> fetch(final Request request, final Callback callback) {
    LOGGER.debug("Getting record for: {}", request.getUrl());
    final MysqlFetcherRequest mysqlFetcherRequest = normalize(request).prependHeaders(headers);

    final BasicFuture<Response> future = new BasicFuture<>(new FutureCallback<Response>() {
      @Override
      public void completed(final Response result) {
        callback.completed(request, result);
      }

      @Override
      public void failed(final Exception ex) {
        callback.failed(request, ex);
      }

      @Override
      public void cancelled() {
        callback.cancelled(request);
      }
    });

    try {
      final Record record = fileManager.get(mysqlFetcherRequest);
      LOGGER.debug("Record found with id: {}", record.getId());

      String baseUrl;
      try {
        baseUrl = UrlUtil.getBaseUrl(request);
      } catch (URISyntaxException e) {
        LOGGER.warn("Could not parse base URL: " + request.getUrl());
        baseUrl = request.getUrl();
      }

      final StorageResponse response = new StorageResponse(
          record.getStatusCode(),
          baseUrl,
          record.getResponseContent(),
          getContentType(record),
          record.getResponseHeaders(),
          String.valueOf(record.getId())
      );

      final Validator.Status status = validator.isValid(Unwrappable.unwrapRequest(request), response);
      if (status != Validator.Status.VALID) {
        future.failed(new ValidationException(status, response, "Invalid response."));
        return future;
      }

      final Response validatedResponse = new StorageResponse(response, validator);
      future.completed(validatedResponse);
      return future;
    } catch (MalformedURLException e) {
      LOGGER.warn("Could not parse base URL: " + request.getUrl());
      future.failed(e);
      return future;
    } catch (IOException e) {
      LOGGER.error("Fail to parse content from storage", e);
      future.failed(e);
      return future;
    } catch (StorageException e) {
      LOGGER.warn("No content found from storage for: {}", request.getUrl());
      future.cancel();
      return future;
    }
  }

  @Override
  public void close() throws Exception {
    if (fileManager != null) {
      fileManager.close();
    }
  }

  /**
   * A builder for MySQL fetcher class.
   */
  public static final class Builder {

    /**
     * The file manager used to store raw responses.
     */
    private final FileManager fileManager;

    /**
     * A list of headers to append to request.
     */
    private Map<String, String> headers;

    /**
     * The validator used.
     */
    private Validator validator;

    /**
     * Construct an instance of builder.
     *
     * @param fileManager an instance file manager used to store raw responses.
     */
    private Builder(final FileManager fileManager) {
      this.fileManager = fileManager;
      headers = Collections.emptyMap();
      validator = new PipelineValidator(
          StatusOkValidator.INSTANCE,
          EmptyContentValidator.INSTANCE
      );
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
     * Builds the fetcher with the options specified.
     *
     * @return an instance of Fetcher.
     */
    public MysqlFetcher build() {
      return new MysqlFetcher(this);
    }

  }
}
