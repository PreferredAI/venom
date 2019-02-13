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
 * @author Ween Jiann Lee
 */
public class StorageResponse implements Response, Retrievable {

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
   * @param baseUrl     Status code of the response
   * @param content     Content from the response
   * @param contentType Content type of the response
   * @param headers     Headers from the response
   * @param sourceId    Stored ID of the response
   */
  public StorageResponse(final int statusCode, final String baseUrl, final byte[] content,
                         final ContentType contentType, final Header[] headers, final String sourceId) {
    this(statusCode, baseUrl, content, contentType, headers, sourceId, null);
  }

  /**
   * Constructs a base response.
   *
   * @param response  an instance of storage response where validator is to be replaced
   * @param validator Validator used to validate this response
   */
  public StorageResponse(final StorageResponse response, final Validator validator) {
    this(response.getStatusCode(), response.getBaseUrl(), response.getContent(), response.getContentType(),
        response.getHeaders(), response.getSourceId(), validator);
  }

  /**
   * Constructs a base response.
   *
   * @param statusCode  Status code of the response
   * @param baseUrl     Status code of the response
   * @param content     Content from the response
   * @param contentType Content type of the response
   * @param headers     Headers from the response
   * @param sourceId    Stored ID of the response
   * @param validator   Validator used to validate this response
   */
  private StorageResponse(final int statusCode, final String baseUrl, final byte[] content,
                          final ContentType contentType, final Header[] headers, final String sourceId,
                          final Validator validator) {
    this.statusCode = statusCode;
    this.baseUrl = baseUrl;
    this.content = content;
    this.contentType = contentType;
    this.headers = headers;
    this.sourceId = sourceId;
    this.validator = validator;
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
  public final Validator getValidator() {
    return validator;
  }

  @Override
  public final HttpHost getProxy() {
    return null;
  }

  @Override
  public final String getSourceId() {
    return sourceId;
  }
}
