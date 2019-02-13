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

import ai.preferred.venom.storage.Record;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;


/**
 * @author Ween Jiann Lee
 */
public class StorageResponse implements Response, Retrievable {

  /**
   * The record holding this response.
   */
  private final Record record;

  /**
   * The base url of this response.
   */
  private final String baseUrl;

  /**
   * Constructs a base response.
   *
   * @param record  record holding this response
   * @param baseUrl base URL of the response
   */
  public StorageResponse(final Record record, final String baseUrl) {
    this.record = record;
    this.baseUrl = baseUrl;
  }

  @Override
  public final int getStatusCode() {
    return record.getStatusCode();
  }

  @Override
  public final byte[] getContent() {
    return record.getResponseContent();
  }

  @Override
  public final ContentType getContentType() {
    return record.getContentType();
  }

  @Override
  public final Header[] getHeaders() {
    return record.getResponseHeaders();
  }

  @Override
  public final String getBaseUrl() {
    return baseUrl;
  }

  @Override
  public final HttpHost getProxy() {
    return null;
  }

  @Override
  public final Record getRecord() {
    return record;
  }
}
