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

  private final int statusCode;

  private final byte[] content;

  private final ContentType contentType;

  private final Header[] headers;

  private final String baseUrl;

  private final HttpHost proxy;

  private final Validator validator;

  private final String sourceId;

  public BaseResponse(int statusCode, String baseUrl, byte[] content, ContentType contentType, Header[] headers,
                      HttpHost proxy) {
    this(statusCode, baseUrl, content, contentType, headers, proxy, null, null);
  }

  public BaseResponse(int statusCode, String baseUrl, byte[] content, ContentType contentType, Header[] headers,
                      HttpHost proxy, Validator validator, String sourceId) {
    this.statusCode = statusCode;
    this.baseUrl = baseUrl;
    this.content = content;
    this.contentType = contentType;
    this.headers = headers;
    this.proxy = proxy;
    this.validator = validator;
    this.sourceId = sourceId;
  }

  public BaseResponse setSourceId(String sourceId) {
    return new BaseResponse(statusCode, baseUrl, content, contentType, headers, proxy, validator, sourceId);
  }

  public BaseResponse setValidator(Validator validator) {
    return new BaseResponse(statusCode, baseUrl, content, contentType, headers, proxy, validator, sourceId);
  }

  @Override
  public int getStatusCode() {
    return statusCode;
  }

  @Override
  public byte[] getContent() {
    return content;
  }

  @Override
  public ContentType getContentType() {
    return contentType;
  }

  @Override
  public Header[] getHeaders() {
    return headers;
  }

  @Override
  public String getBaseUrl() {
    return baseUrl;
  }

  @Override
  public HttpHost getProxy() {
    return proxy;
  }

  @Override
  public Validator getValidator() {
    return validator;
  }

  @Override
  public String getSourceId() {
    return sourceId;
  }

}
