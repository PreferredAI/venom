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

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * @author Maksim Tkachenko
 * @author Truong Quoc Tuan
 * @author Ween Jiann Lee
 */
public interface Response {

  /**
   * Returns status code of the response.
   *
   * @return int code
   */
  int getStatusCode();

  /**
   * Returns raw content of the response.
   *
   * @return byte[] content
   */
  byte[] getContent();

  /**
   * Returns the content type of the content fetched.
   * <p>
   * This is provided by the server or guessed by the server or an
   * amalgamation of both.
   * </p>
   *
   * @return an instance of ContentType
   */
  @NotNull
  ContentType getContentType();

  /**
   * Returns the headers that were used to trigger this response.
   *
   * @return an array of headers
   */
  @NotNull
  Header[] getHeaders();

  /**
   * Returns the base form of the url used in this request.
   *
   * @return stripped down version of requested url
   */
  @NotNull
  String getBaseUrl();

  /**
   * Returns the proxy that was used to trigger this response.
   *
   * @return proxy used
   */
  @Nullable
  HttpHost getProxy();

}
