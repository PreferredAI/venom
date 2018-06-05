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

import ai.preferred.venom.utils.UrlUtils;
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

  public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  private final Response innerResponse;

  public VResponse(Response response) {
    this.innerResponse = response;
  }

  @Override
  public int getStatusCode() {
    return getInner().getStatusCode();
  }

  public byte[] getContent() {
    return getInner().getContent();
  }

  @Override
  public ContentType getContentType() {
    return getInner().getContentType();
  }

  @Override
  public Header[] getHeaders() {
    return getInner().getHeaders();
  }

  @Override
  public String getBaseUrl() {
    return getInner().getBaseUrl();
  }

  @Override
  public HttpHost getProxy() {
    return getInner().getProxy();
  }

  @Override
  public Validator getValidator() {
    return getInner().getValidator();
  }

  /**
   * Returns the html in string format with all relative
   * urls resolved to absolute urls
   *
   * @return string of html response
   */
  public String getResolvedHtml() {
    return UrlUtils.resolveUrls(getHtml(), getBaseUrl());
  }

  /**
   * Returns the html in string format with all relative
   * urls resolved to absolute urls
   *
   * @param charset use specified charset for this html document
   * @return string of html response
   */
  public String getResolvedHtml(Charset charset) {
    return UrlUtils.resolveUrls(getHtml(charset), getBaseUrl());
  }

  /**
   * Returns the html in string format
   *
   * @return string of html response
   */
  public String getHtml() {
    final Charset charset = getContentType().getCharset();
    if (charset == null) return getHtml(DEFAULT_CHARSET);
    return getHtml(charset);
  }

  /**
   * Returns the html in string format
   *
   * @param charset use specified charset for this html document
   * @return string of html response
   */
  public String getHtml(Charset charset) {
    return new String(getContent(), charset);
  }

  /**
   * Returns a Jsoup document of this response
   *
   * @return Jsoup document of response
   */
  public Document getJsoup() {
    return Jsoup.parse(getHtml(), getBaseUrl());
  }

  /**
   * Returns a Jsoup document of this response
   *
   * @param charset use specified charset for this html document
   * @return Jsoup document of response
   */
  public Document getJsoup(Charset charset) {
    return Jsoup.parse(getHtml(charset), getBaseUrl());
  }

  @Override
  public Response getInner() {
    return innerResponse;
  }
}
