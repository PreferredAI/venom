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

/**
 * @author Maksim Tkachenko
 * @author Truong Quoc Tuan
 * @author Ween Jiann Lee
 */
public class BaseResponse implements Response, Retrievable {

  /**
   * The status code of this response.
   */
  private final int statusCode;

  /**
   * The content of this response.
   */
  private final byte[] content;

  /**
   * The content type of this response.
   */
  private final ContentType contentType;

  /**
   * The headers of this response.
   */
  private final Header[] headers;

  /**
   * The base url of this response.
   */
  private final String baseUrl;

  /**
   * The proxy used to obtain response.
   */
  private final HttpHost proxy;

  /**
   * The validator used to validate this response.
   */
  private final Validator validator;

  /**
   * The source id of this response.
   */
  private final String sourceId;

  /**
   * Constructs a base response.
   *
   * @param statusCode  Status code of the response
   * @param baseUrl     Base url of the response
   * @param content     Content from the response
   * @param contentType Content type of the response
   * @param headers     Headers from the response
   * @param proxy       Proxy used to obtain the response
   */
  public BaseResponse(final int statusCode, final String baseUrl, final byte[] content, final ContentType contentType,
                      final Header[] headers, final HttpHost proxy) {
    this(statusCode, baseUrl, content, contentType, headers, proxy, null, null);
  }

  /**
   * Constructs a base response.
   *
   * @param statusCode  Status code of the response
   * @param baseUrl     Base url of the response
   * @param content     Content from the response
   * @param contentType Content type of the response
   * @param headers     Headers from the response
   * @param proxy       Proxy used to obtain the response
   * @param validator   Validator used to validate this response
   * @param sourceId    `id` of the row the raw response is saved to
   */
  public BaseResponse(final int statusCode, final String baseUrl, final byte[] content, final ContentType contentType,
                      final Header[] headers, final HttpHost proxy, final Validator validator, final String sourceId) {
    this.statusCode = statusCode;
    this.baseUrl = baseUrl;
    this.content = content;
    this.contentType = contentType;
    this.headers = headers;
    this.proxy = proxy;
    this.validator = validator;
    this.sourceId = sourceId;
  }

  @Override
  public final int getStatusCode() {
    return statusCode;
  }

  @Override
  public final byte[] getContent() {
    return content;
  }

  @Override
  public final ContentType getContentType() {
    return contentType;
  }

  @Override
  public final Header[] getHeaders() {
    return headers;
  }

  @Override
  public final String getBaseUrl() {
    return baseUrl;
  }

  @Override
  public final HttpHost getProxy() {
    return proxy;
  }

  @Override
  public final Validator getValidator() {
    return validator;
  }

  /**
   * Sets the validator used to validate this response.
   *
   * @param validator Row id of the saved response
   * @return A new instance of base response
   */
  public final BaseResponse setValidator(final Validator validator) {
    return new BaseResponse(statusCode, baseUrl, content, contentType, headers, proxy, validator, sourceId);
  }

  @Override
  public final String getSourceId() {
    return sourceId;
  }

  /**
   * Sets the source id where the raw response is saved.
   *
   * @param sourceId Row id of the saved response
   * @return A new instance of base response
   */
  public final BaseResponse setSourceId(final String sourceId) {
    return new BaseResponse(statusCode, baseUrl, content, contentType, headers, proxy, validator, sourceId);
  }

}
