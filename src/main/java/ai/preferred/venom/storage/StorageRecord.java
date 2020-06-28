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

package ai.preferred.venom.storage;

import ai.preferred.venom.request.Request;
import org.apache.http.Header;
import org.apache.http.entity.ContentType;

import java.util.Map;

/**
 * This class implements a default storage record.
 *
 * @param <T> the type of id
 * @author Ween Jiann Lee
 */
public final class StorageRecord<T> implements Record<T> {

  /**
   * The id of this record.
   */
  private final T id;

  /**
   * The url for this request.
   */
  private final String url;

  /**
   * The method used for the request.
   */
  private final Request.Method requestMethod;

  /**
   * The headers used for the request.
   */
  private final Map<String, String> requestHeaders;

  /**
   * The body of the request.
   */
  private final Map<String, String> requestBody;

  /**
   * The status code of the response.
   */
  private final int statusCode;

  /**
   * The headers of the response.
   */
  private final Header[] responseHeaders;

  /**
   * The content type of the response.
   */
  private final ContentType contentType;

  /**
   * The content of the response.
   */
  private final byte[] responseContent;

  /**
   * The md5 hash of the content.
   */
  private final String md5;

  /**
   * The date this record was created.
   */
  private final long dateCreated;

  /**
   * Constructs an instance of StorageRecord.
   *
   * @param builder an instance of builder
   */
  private StorageRecord(final Builder<T> builder) {
    this.id = builder.id;
    this.url = builder.url;
    this.requestMethod = builder.requestMethod;
    this.requestHeaders = builder.requestHeaders;
    this.requestBody = builder.requestBody;
    this.statusCode = builder.statusCode;
    this.responseHeaders = builder.responseHeaders;
    this.contentType = builder.contentType;
    this.responseContent = builder.responseContent;
    this.md5 = builder.md5;
    this.dateCreated = builder.dateCreated;
  }

  /**
   * Create an instance of builder.
   *
   * @param <T> the type of id
   * @param id  id for the record
   * @return a new instance of builder
   */
  public static <T> Builder<T> builder(T id) {
    return new Builder<>(id);
  }

  @Override
  public T getId() {
    return id;
  }

  @Override
  public String getURL() {
    return url;
  }

  @Override
  public Request.Method getRequestMethod() {
    return requestMethod;
  }

  @Override
  public Map<String, String> getRequestHeaders() {
    return requestHeaders;
  }

  @Override
  public Map<String, String> getRequestBody() {
    return requestBody;
  }

  @Override
  public int getStatusCode() {
    return statusCode;
  }

  @Override
  public Header[] getResponseHeaders() {
    return responseHeaders;
  }

  @Override
  public ContentType getContentType() {
    return contentType;
  }

  @Override
  public byte[] getResponseContent() {
    return responseContent;
  }

  @Override
  public long getDateCreated() {
    return dateCreated;
  }

  /**
   * Get md5 hash of the response content.
   *
   * @return md5 hash of the response content
   */
  public String getMD5() {
    return md5;
  }

  /**
   * A builder for StorageRecord class.
   *
   * @param <T> the type of id
   */
  public static final class Builder<T> {

    /**
     * The id of this record.
     */
    private final T id;

    /**
     * The url for this request.
     */
    private String url;

    /**
     * The method used for the request.
     */
    private Request.Method requestMethod;

    /**
     * The headers used for the request.
     */
    private Map<String, String> requestHeaders;

    /**
     * The body of the request.
     */
    private Map<String, String> requestBody;

    /**
     * The status code of the response.
     */
    private int statusCode;

    /**
     * The headers of the response.
     */
    private Header[] responseHeaders;

    /**
     * The content type of the response.
     */
    private ContentType contentType;

    /**
     * The content of the response.
     */
    private byte[] responseContent;

    /**
     * The md5 hash of the content.
     */
    private String md5;

    /**
     * The date this record was created.
     */
    private long dateCreated;

    /**
     * Constructs an instance of StorageRecord.Builder.
     *
     * @param id id for the record
     */
    private Builder(T id) {
      this.id = id;
    }

    /**
     * Sets the url for the record.
     *
     * @param url url for the request
     * @return this
     */
    public Builder<T> setUrl(final String url) {
      this.url = url;
      return this;
    }

    /**
     * Sets the request method for the record.
     *
     * @param requestMethod method of the request
     * @return this
     */
    public Builder<T> setRequestMethod(final Request.Method requestMethod) {
      this.requestMethod = requestMethod;
      return this;
    }

    /**
     * Sets the request headers for the record.
     *
     * @param requestHeaders headers of the request
     * @return this
     */
    public Builder<T> setRequestHeaders(final Map<String, String> requestHeaders) {
      this.requestHeaders = requestHeaders;
      return this;
    }

    /**
     * Sets the request body for the record.
     *
     * @param requestBody body of the request
     * @return this
     */
    public Builder<T> setRequestBody(final Map<String, String> requestBody) {
      this.requestBody = requestBody;
      return this;
    }

    /**
     * Sets the response status code for the record.
     *
     * @param statusCode status code of the response
     * @return this
     */
    public Builder<T> setStatusCode(final int statusCode) {
      this.statusCode = statusCode;
      return this;
    }

    /**
     * Sets the response headers for the record.
     *
     * @param responseHeaders headers of the response
     * @return this
     */
    public Builder<T> setResponseHeaders(final Header[] responseHeaders) {
      this.responseHeaders = responseHeaders;
      return this;
    }

    /**
     * Sets the response content type for the record.
     *
     * @param contentType content type of the response
     * @return this
     */
    public Builder<T> setContentType(final ContentType contentType) {
      this.contentType = contentType;
      return this;
    }

    /**
     * Sets the response content for the record.
     *
     * @param responseContent content of the response
     * @return this
     */
    public Builder<T> setResponseContent(final byte[] responseContent) {
      this.responseContent = responseContent;
      return this;
    }

    /**
     * Sets the md5 hash of the response content for the record.
     *
     * @param md5 md5 hash of the response content
     * @return this
     */
    public Builder<T> setMD5(final String md5) {
      this.md5 = md5;
      return this;
    }

    /**
     * Sets the date the record was created.
     *
     * @param dateCreated date created of the record
     * @return this
     */
    public Builder<T> setDateCreated(final long dateCreated) {
      this.dateCreated = dateCreated;
      return this;
    }

    /**
     * Builds the storage record with the details specified.
     *
     * @return an instance of StorageRecord.
     */
    public StorageRecord<T> build() {
      return new StorageRecord<>(this);
    }

  }

}
