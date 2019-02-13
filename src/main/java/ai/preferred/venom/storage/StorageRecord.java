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
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * This class implements a default storage record.
 *
 * @author Ween Jiann Lee
 */
public final class StorageRecord implements Record {

  /**
   * The id of this record.
   */
  private final int id;

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
   * The mime type of the response.
   */
  private final String mimeType;

  /**
   * The encoding of the response.
   */
  private final Charset encoding;

  /**
   * The content of the response.
   */
  private final File responseContent;

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
  private StorageRecord(final Builder builder) {
    this.id = builder.id;
    this.url = builder.url;
    this.requestMethod = builder.requestMethod;
    this.requestHeaders = builder.requestHeaders;
    this.requestBody = builder.requestBody;
    this.statusCode = builder.statusCode;
    this.responseHeaders = builder.responseHeaders;
    this.mimeType = builder.mimeType;
    this.encoding = builder.encoding;
    this.responseContent = builder.responseContent;
    this.md5 = builder.md5;
    this.dateCreated = builder.dateCreated;
  }

  /**
   * Create an instance of builder.
   *
   * @return A new instance of builder
   */
  public static Builder builder() {
    return new Builder();
  }

  @Override
  public int getId() {
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
  public String getMimeType() {
    return mimeType;
  }

  @Override
  public Charset getEncoding() {
    return encoding;
  }

  @Override
  public byte[] getResponseContent() throws IOException {
    return IOUtils.toByteArray(getStreamResponseContent());
  }

  @Override
  public InputStream getStreamResponseContent() throws IOException {
    return new BufferedInputStream(
        new GZIPInputStream(
            new FileInputStream(responseContent)
        )
    );
  }

  @Override
  public long getDateCreated() {
    return dateCreated;
  }

  public String getMD5() {
    return md5;
  }

  /**
   * A builder for StorageRecord class.
   */
  public static class Builder {

    /**
     * The id of this record.
     */
    private int id;

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
     * The mime type of the response.
     */
    private String mimeType;

    /**
     * The encoding of the response.
     */
    private Charset encoding;

    /**
     * The content of the response.
     */
    private File responseContent;

    /**
     * The md5 hash of the content.
     */
    private String md5;

    /**
     * The date this record was created.
     */
    private long dateCreated;

    /**
     * Sets the id for the record.
     *
     * @param id id for the record
     * @return this
     */
    public final Builder setId(final int id) {
      this.id = id;
      return this;
    }

    /**
     * Sets the url for the record.
     *
     * @param url url for the request
     * @return this
     */
    public final Builder setUrl(final String url) {
      this.url = url;
      return this;
    }

    /**
     * Sets the request method for the record.
     *
     * @param requestMethod method of the request
     * @return this
     */
    public final Builder setRequestMethod(final Request.Method requestMethod) {
      this.requestMethod = requestMethod;
      return this;
    }

    /**
     * Sets the request headers for the record.
     *
     * @param requestHeaders headers of the request
     * @return this
     */
    public final Builder setRequestHeaders(final Map<String, String> requestHeaders) {
      this.requestHeaders = requestHeaders;
      return this;
    }

    /**
     * Sets the request body for the record.
     *
     * @param requestBody body of the request
     * @return this
     */
    public final Builder setRequestBody(final Map<String, String> requestBody) {
      this.requestBody = requestBody;
      return this;
    }

    /**
     * Sets the respose status code for the record.
     *
     * @param statusCode status code of the response
     * @return this
     */
    public final Builder setStatusCode(final int statusCode) {
      this.statusCode = statusCode;
      return this;
    }

    /**
     * Sets the response headers for the record.
     *
     * @param responseHeaders headers of the response
     * @return this
     */
    public final Builder setResponseHeaders(final Header[] responseHeaders) {
      this.responseHeaders = responseHeaders;
      return this;
    }

    /**
     * Sets the response mime type for the record.
     *
     * @param mimeType mime type of the response
     * @return this
     */
    public final Builder setMimeType(final String mimeType) {
      this.mimeType = mimeType;
      return this;
    }

    /**
     * Sets the response encoding for the record.
     *
     * @param encoding encoding of the response
     * @return this
     */
    public final Builder setEncoding(final Charset encoding) {
      this.encoding = encoding;
      return this;
    }

    /**
     * Sets the response content for the record.
     *
     * @param responseContent content of the response
     * @return this
     */
    public final Builder setResponseContent(final File responseContent) {
      this.responseContent = responseContent;
      return this;
    }

    /**
     * Sets the md5 hash of the response content for the record.
     *
     * @param md5 md5 hash of the response content
     * @return this
     */
    public final Builder setMD5(final String md5) {
      this.md5 = md5;
      return this;
    }

    /**
     * Sets the date the record was created.
     *
     * @param dateCreated date created of the record
     * @return this
     */
    public final Builder setDateCreated(final long dateCreated) {
      this.dateCreated = dateCreated;
      return this;
    }

    /**
     * Builds the storage record with the details specified.
     *
     * @return an instance of StorageRecord.
     */
    public final StorageRecord build() {
      return new StorageRecord(this);
    }

  }

}
