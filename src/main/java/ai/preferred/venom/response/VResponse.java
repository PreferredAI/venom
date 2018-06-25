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

package ai.preferred.venom.response;

import ai.preferred.venom.validator.Validator;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author Maksim Tkachenko
 * @author Truong Quoc Tuan
 * @author Ween Jiann Lee
 */
public class VResponse implements Response, Unwrappable {

  /**
   * The default charset to be used to decode response.
   */
  public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  /**
   * An instance of underlying response.
   */
  private final Response innerResponse;

  /**
   * Constructs a VResponse.
   *
   * @param response An instance of response
   */
  public VResponse(final Response response) {
    this.innerResponse = response;
  }

  @Override
  public final int getStatusCode() {
    return getInner().getStatusCode();
  }

  @Override
  public final byte[] getContent() {
    return getInner().getContent();
  }

  @Override
  public final ContentType getContentType() {
    return getInner().getContentType();
  }

  @Override
  public final Header[] getHeaders() {
    return getInner().getHeaders();
  }

  @Override
  public final String getBaseUrl() {
    return getInner().getBaseUrl();
  }

  @Override
  public final HttpHost getProxy() {
    return getInner().getProxy();
  }

  @Override
  public final Validator getValidator() {
    return getInner().getValidator();
  }

  /**
   * Returns the html in string format.
   *
   * @return string of html response
   */
  public final String getHtml() {
    final Charset charset = getContentType().getCharset();
    if (charset == null) {
      return getHtml(DEFAULT_CHARSET);
    }
    return getHtml(charset);
  }

  /**
   * Returns the html in string format.
   *
   * @param charset use specified charset for this html document
   * @return string of html response
   */
  public final String getHtml(final Charset charset) {
    return new String(getContent(), charset);
  }

  /**
   * Returns a Jsoup document of this response.
   *
   * @return Jsoup document of response
   */
  public final Document getJsoup() {
    return Jsoup.parse(getHtml(), getBaseUrl());
  }

  /**
   * Returns a Jsoup document of this response.
   *
   * @param charset use specified charset for this html document
   * @return Jsoup document of response
   */
  public final Document getJsoup(final Charset charset) {
    return Jsoup.parse(getHtml(charset), getBaseUrl());
  }

  @Override
  public final Response getInner() {
    return innerResponse;
  }
}
