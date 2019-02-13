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

import ai.preferred.venom.fetcher.Callback;
import ai.preferred.venom.request.Request;
import ai.preferred.venom.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the default callback for file managers.
 *
 * @author Ween Jiann Lee
 */
public class FileManagerCallback implements Callback {

  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(FileManagerCallback.class);

  /**
   * The file manager used to store raw responses.
   */
  private final FileManager fileManager;

  /**
   * Constructs an instance of file manager callback.
   *
   * @param fileManager an instance of file manager used to store raw responses
   */
  public FileManagerCallback(final FileManager fileManager) {
    this.fileManager = fileManager;
  }

  @Override
  public final void completed(final Request request, final Response response) {
    try {
      fileManager.put(request, response);
    } catch (StorageException e) {
      LOGGER.error("Unable to store response for {}", request.getUrl(), e);
    }
  }

  @Override
  public final void failed(final Request request, final Exception ex) {
    // do nothing
  }

  @Override
  public final void cancelled(final Request request) {
    // do nothing
  }

}
