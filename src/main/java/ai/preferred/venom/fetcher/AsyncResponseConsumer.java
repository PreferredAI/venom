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

  private static final Logger LOGGER = LoggerFactory.getLogger(AsyncResponseConsumer.class);

  private static final ResponseDecompressor RESPONSE_DECOMPRESSOR = new ResponseDecompressor();

  private static final ContentType DEFAULT_CONTENT_TYPE = ContentType.APPLICATION_OCTET_STREAM;

  private final Validator validator;

  private final Set<Integer> stopCodes;

  private final boolean compressed;

  private final Request request;

  private volatile HttpResponse httpResponse;

  private volatile SimpleInputBuffer buf;

  public AsyncResponseConsumer(Validator validator, Set<Integer> stopCodes, boolean compressed,
                               HttpFetcherRequest request) {
    this.validator = validator;
    this.stopCodes = stopCodes;
    this.compressed = compressed;
    this.request = request;
  }

  private BaseResponse createVenomResponse(boolean compressed) throws IOException {
    if (compressed) {
      RESPONSE_DECOMPRESSOR.decompress(httpResponse);
    }

    final byte[] content = EntityUtils.toByteArray(httpResponse.getEntity());
    final Header[] headers = httpResponse.getAllHeaders();
    final ContentType contentType = parseContentType(content);

    String baseUrl = "";
    try {
      URL url = new URL(request.getUrl());
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

  private ContentType parseContentType(byte[] content) {
    try {
      ContentType type = ContentType.get(httpResponse.getEntity());
      if (type == null) {
        TikaInputStream stream = TikaInputStream.get(new ByteArrayInputStream(content));
        Tika tika = new Tika();
        String fileType = tika.detect(stream);
        type = ContentType.create(fileType);
      }
      if (type.getCharset() == null) {
        CharsetMatch match = new CharsetDetector()
            .setText(new ByteArrayInputStream(content))
            .detect();

        if (match != null && match.getConfidence() > 50) {
          type = type.withCharset(match.getName());
        }
      }
      return type;
    } catch (ParseException e) {
      LOGGER.warn("Could not parse content type", e);
    } catch (UnsupportedCharsetException e) {
      LOGGER.warn("Charset is not available in this instance of the Java virtual machine", e);
    } catch (IOException e) {
      LOGGER.warn("Cannot get content to determine media type", e);
    }
    return DEFAULT_CONTENT_TYPE;
  }

  @Override
  protected void onResponseReceived(final HttpResponse httpResponse) {
    this.httpResponse = httpResponse;
  }

  @Override
  protected void onContentReceived(
      final ContentDecoder decoder, final IOControl ioctrl) throws IOException {
    Asserts.notNull(this.buf, "Content buffer");
    this.buf.consumeContent(decoder);
  }

  @Override
  protected void onEntityEnclosed(
      final HttpEntity entity, final ContentType contentType) throws IOException {
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
  protected BaseResponse buildResult(HttpContext context) throws Exception {
    final int statusCode = httpResponse.getStatusLine().getStatusCode();
    if (stopCodes.contains(statusCode)) {
      EntityUtils.consumeQuietly(httpResponse.getEntity());
      releaseResources();
      throw new StopCodeException(statusCode, "Stop code received.");
    }

    final BaseResponse response = createVenomResponse(compressed);
    releaseResources();

    try {
      final Validator.Status status = validator.isValid(Unwrappable.unwrapRequest(request), response);
      if (status != Validator.Status.VALID) {
        throw new ValidationException(status, response, "Invalid response.");
      }
    } catch (Exception e) {
      throw new ValidationException(Validator.Status.INVALID_CONTENT, response, "Validator threw an exception, " +
          "please check your code for bugs.", e);
    }

    return response;
  }

  @Override
  protected void releaseResources() {
    this.httpResponse = null;
    this.buf = null;
  }
}
