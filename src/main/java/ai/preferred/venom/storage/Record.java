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

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author Maksim Tkachenko
 * @author Truong Quoc Tuan
 * @author Ween Jiann Lee
 */
public interface Record {

  /**
   * @return valid id if the record is stored, null otherwise
   */
  int getId();

  /**
   * @return URL of the stored content
   */
  @NotNull String getURL();

  /**
   * @return Request type
   */
  @NotNull Request.Method getRequestMethod();

  /**
   * @return map of request headers
   */
  @Nullable
  Map<String, String> getRequestHeaders();

  /**
   * @return map of request body
   */
  @Nullable
  Map<String, String> getRequestBody();

  /**
   * @return status code
   */
  int getStatusCode();

  /**
   * @return BaseResponse headers
   */
  @Nullable
  Header[] getResponseHeaders();

  /**
   * @return raw response file (uncompressed)
   * @throws IOException throws IOException
   */
  byte[] getResponseContent() throws IOException;

  /**
   * @return response file (uncompressed)
   * @throws IOException throws IOException
   */
  @NotNull
  InputStream getStreamResponseContent() throws IOException;

  /**
   * @return md5 hash of the input stream
   * @throws IOException throws IOException
   */
  @NotNull
  String getMD5() throws IOException;

  /**
   * @return valid timestamp if the record is stored, -1 otherwise
   */
  long getDateCreated();

}
