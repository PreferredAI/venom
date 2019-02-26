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

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * This interface represents only the most basic of a record and
 * the fields that should be retrievable from database.
 *
 * @param <T> the type of id
 * @author Maksim Tkachenko
 * @author Truong Quoc Tuan
 * @author Ween Jiann Lee
 */
public interface Record<T> {

  /**
   * @return valid id if the record is stored, null otherwise
   */
  T getId();

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
   * @return ContentType of the content
   */
  ContentType getContentType();

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
   */
  byte[] getResponseContent();

  /**
   * @return valid timestamp if the record is stored, -1 otherwise
   */
  long getDateCreated();

}
