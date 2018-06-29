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

import ai.preferred.venom.request.HttpFetcherRequest;
import ai.preferred.venom.request.Request;
import ai.preferred.venom.request.Unwrappable;
import ai.preferred.venom.response.BaseResponse;
import ai.preferred.venom.response.Response;
import ai.preferred.venom.utils.ResponseDecompressor;
import ai.preferred.venom.validator.Validator;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import org.apache.http.*;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.entity.ContentBufferEntity;
import org.apache.http.nio.protocol.AbstractAsyncResponseConsumer;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.nio.util.SimpleInputBuffer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Asserts;
import org.apache.http.util.EntityUtils;
import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Set;

/**
 * On top of the abstract class, this class handles the parsing of a response
 * from the web service.
 *
 * @author Ween Jiann Lee
 */
public class AsyncResponseConsumer extends AbstractAsyncResponseConsumer<Response> {

  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(AsyncResponseConsumer.class);

  /**
   * Decompressor used to decompress responses.
   */
  private static final ResponseDecompressor RESPONSE_DECOMPRESSOR = new ResponseDecompressor();

  /**
   * Default content type of response if not given.
   */
  private static final ContentType DEFAULT_CONTENT_TYPE = ContentType.APPLICATION_OCTET_STREAM;

  /**
   * The validator to be use to validate this response.
   */
  private final Validator validator;

  /**
   * A set of stop codes to interrupt crawling.
   */
  private final Set<Integer> stopCodes;

  /**
   * Determines whether responses might be compressed.
   */
  private final boolean compressed;

  /**
   * The request leading to this response.
   */
  private final Request request;

  /**
   * An instance of http response.
   */
  private volatile HttpResponse httpResponse;

  /**
   * A buffer for the content.
   */
  private volatile SimpleInputBuffer buf;

  /**
   * Lazy loaded byte[] content of http entity.
   */
  private byte[] byteContent;

  /**
   * Lazy loaded content type of http entity.
   */
  private ContentType contentType;

  /**
   * Constructs an instance of async response consumer.
   *
   * @param validator  The instance of validator to be used
   * @param stopCodes  A set of stop code to interrupt crawling
   * @param compressed Determines whether responses might be compressed
   * @param request    The request leading to this response
   */
  AsyncResponseConsumer(final Validator validator, final Set<Integer> stopCodes, final boolean compressed,
                        final HttpFetcherRequest request) {
    this.validator = validator;
    this.stopCodes = stopCodes;
    this.compressed = compressed;
    this.request = request;
  }

  /**
   * Get byte[] content of the http entity.
   *
   * @param entity An instance of http entity
   * @return Byte array of http entity
   * @throws IOException If unable to get byte array
   */
  private synchronized byte[] getByteContent(final HttpEntity entity) throws IOException {
    if (this.byteContent == null) {
      this.byteContent = EntityUtils.toByteArray(entity);
    }
    return this.byteContent;
  }

  /**
   * Create an instance of venom response.
   *
   * @param compressed Determines whether responses might be compressed
   * @return An instance of base response
   * @throws IOException Reading http response
   */
  private BaseResponse createVenomResponse(final boolean compressed) throws IOException {
    if (compressed) {
      RESPONSE_DECOMPRESSOR.decompress(httpResponse);
    }

    final HttpEntity entity = httpResponse.getEntity();
    final byte[] content = getByteContent(entity);
    final ContentType contentType = getContentType(entity);
    final Header[] headers = httpResponse.getAllHeaders();

    String baseUrl = "";
    try {
      final URL url = new URL(request.getUrl());
      baseUrl = url.getProtocol() + "://" + url.getHost();
    } catch (MalformedURLException e) {
      LOGGER.warn("Could not parse base URL: " + request.getUrl());
    }

    return new BaseResponse(
        httpResponse.getStatusLine().getStatusCode(),
        baseUrl,
        content,
        contentType,
        headers,
        request.getProxy());
  }

  @Override
  protected final synchronized ContentType getContentType(final HttpEntity entity) {
    if (this.contentType == null) {
      try {
        ContentType contentType = ContentType.get(entity);
        if (contentType == null) {
          final Tika tika = new Tika();
          final TikaInputStream stream = TikaInputStream.get(
              new ByteArrayInputStream(getByteContent(entity))
          );
          final String fileType = tika.detect(stream);
          contentType = ContentType.create(fileType);
        }
        if (contentType.getCharset() == null) {
          final CharsetMatch match = new CharsetDetector()
              .setText(entity.getContent())
              .detect();

          if (match != null && match.getConfidence() > 50) {
            contentType = contentType.withCharset(match.getName());
          }
        }
        this.contentType = contentType;
      } catch (ParseException e) {
        LOGGER.warn("Could not parse content type", e);
      } catch (UnsupportedCharsetException e) {
        LOGGER.warn("Charset is not available in this instance of the Java virtual machine", e);
      } catch (IOException e) {
        LOGGER.warn("Cannot get content to determine media type", e);
      }
      this.contentType = DEFAULT_CONTENT_TYPE;
    }
    return this.contentType;
  }

  @Override
  protected final void onResponseReceived(final HttpResponse httpResponse) {
    this.httpResponse = httpResponse;
  }

  @Override
  protected final void onContentReceived(final ContentDecoder decoder, final IOControl ioctrl) throws IOException {
    Asserts.notNull(this.buf, "Content buffer");
    this.buf.consumeContent(decoder);
  }

  @Override
  protected final void onEntityEnclosed(final HttpEntity entity, final ContentType contentType) throws IOException {
    long len = entity.getContentLength();
    if (len > Integer.MAX_VALUE) {
      throw new ContentTooLongException("Entity content is too long: " + len);
    }
    if (len < 0) {
      len = 4096;
    }
    this.buf = new SimpleInputBuffer((int) len, new HeapByteBufferAllocator());
    this.httpResponse.setEntity(new ContentBufferEntity(entity, this.buf));
  }

  @Override
  protected final BaseResponse buildResult(final HttpContext context) throws Exception {
    final int statusCode = httpResponse.getStatusLine().getStatusCode();
    if (stopCodes.contains(statusCode)) {
      EntityUtils.consumeQuietly(httpResponse.getEntity());
      releaseResources();
      throw new StopCodeException(statusCode, "Stop code received.");
    }

    final BaseResponse response = createVenomResponse(compressed);
    releaseResources();

    final Validator.Status status;
    try {
      status = validator.isValid(Unwrappable.unwrapRequest(request), response);
    } catch (Exception e) {
      throw new ValidationException(Validator.Status.INVALID_CONTENT, response, "Validator threw an exception, "
          + "please check your code for bugs.", e);
    }

    if (status != Validator.Status.VALID) {
      throw new ValidationException(status, response, "Invalid response.");
    }

    return response;
  }

  @Override
  protected final void releaseResources() {
    this.httpResponse = null;
    this.buf = null;
  }
}
