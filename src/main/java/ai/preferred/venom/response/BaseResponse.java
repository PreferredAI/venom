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

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;

/**
 * @author Maksim Tkachenko
 * @author Truong Quoc Tuan
 * @author Ween Jiann Lee
 */
public class BaseResponse implements Response {

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
  private final String url;

  /**
   * The proxy used to obtain response.
   */
  private final HttpHost proxy;

  /**
   * Constructs a base response.
   *
   * @param statusCode  Status code of the response
   * @param url         Base url of the response
   * @param content     Content from the response
   * @param contentType Content type of the response
   * @param headers     Headers from the response
   * @param proxy       Proxy used to obtain the response
   */
  public BaseResponse(final int statusCode, final String url, final byte[] content, final ContentType contentType,
                      final Header[] headers, final HttpHost proxy) {
    this.statusCode = statusCode;
    this.url = url;
    this.content = content;
    this.contentType = contentType;
    this.headers = headers;
    this.proxy = proxy;
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
  public final String getUrl() {
    return url;
  }

  @Override
  public final String getBaseUrl() {
    return getUrl();
  }

  @Override
  public final HttpHost getProxy() {
    return proxy;
  }

}
