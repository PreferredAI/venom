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

import ai.preferred.venom.response.Response;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

/**
 * A utility for storage needs.
 *
 * @author Ween Jiann Lee
 */
public final class StorageUtil {

  /**
   * Prevent construction of StorageUtil.
   */
  private StorageUtil() {

  }

  /**
   * Get file extension from a response.
   *
   * @param response an instance of response
   * @return file extension for response content
   * @throws MimeTypeException if the given media type name is invalid
   */
  public static String getFileExtension(final Response response) throws MimeTypeException {
    return getFileExtension(response.getContentType().getMimeType());
  }

  /**
   * Get file extension from mime type name.
   *
   * @param mimeTypeStr mime type name
   * @return file extension for response content
   * @throws MimeTypeException if the given media type name is invalid
   */
  public static String getFileExtension(final String mimeTypeStr) throws MimeTypeException {
    final MimeTypes defaultMimeTypes = MimeTypes.getDefaultMimeTypes();
    final MimeType mimeType = defaultMimeTypes.forName(mimeTypeStr);
    return mimeType.getExtension();
  }

}
