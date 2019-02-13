/*
 * Copyright 2017 Preferred.AI
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
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tika.mime.MimeTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * This class implements a FileManager that writes response content to a
 * file on the file system.
 * <p>
 * This implementation is for debugging use and does not support get.
 * </p>
 *
 * @author Truong Quoc Tuan
 * @author Maksim Tkachenko
 * @author Ween Jiann Lee
 */
public class DummyFileManager implements FileManager {

  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(DummyFileManager.class);

  /**
   * The storage path on the file system to use for content storage.
   */
  private final File storagePath;

  /**
   * The callback to trigger upon response.
   */
  private final Callback callback;

  /**
   * Constructs an instance of DummyFileManager.
   *
   * @param storageDir storage directory to use for content storage
   */
  public DummyFileManager(final String storageDir) {
    this(new File(storageDir));
  }

  /**
   * Constructs an instance of DummyFileManager.
   *
   * @param storagePath storage path to use for content storage
   */
  public DummyFileManager(final File storagePath) {
    this.storagePath = storagePath;
    this.callback = new FileManagerCallback(this);
  }

  /**
   * Write stream to file.
   *
   * @param in        an instance of InputStream
   * @param parentDir the directory to save the file
   * @param filename  the filename to write the file
   * @return path to the written file
   * @throws IOException if an I/O error occurs
   */
  private String write(final InputStream in, final File parentDir, final String filename) throws IOException {
    if (!parentDir.exists() && !parentDir.mkdirs()) {
      throw new IOException("Cannot create the dir: " + parentDir);
    }
    if (parentDir.exists() && !parentDir.isDirectory()) {
      throw new IOException("The path is not a dir: " + parentDir);
    }
    final File htmlFile = new File(parentDir, filename);
    try (final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(htmlFile))) {
      IOUtils.copy(in, out);
    }

    return htmlFile.getAbsolutePath();
  }

  @Override
  public final Callback getCallback() {
    return callback;
  }

  @Override
  public final String put(final Request request, final Response response) throws StorageException {
    try {
      final InputStream content = new ByteArrayInputStream(response.getContent());
      final String md5 = DigestUtils.md5Hex(content);
      content.reset();

      final String subDirName = md5.substring(0, 2);

      String tryFileExtension;
      try {
        tryFileExtension = StorageUtil.getFileExtension(response);
      } catch (MimeTypeException e) {
        LOGGER.warn("Cannot find mime type defaulting to no extension");
        tryFileExtension = "";
      }
      final String fileExtension = tryFileExtension;

      LOGGER.info("Response from request {} has been saved to {}", request.getUrl(), md5 + fileExtension);
      return write(content, new File(storagePath, subDirName), md5 + fileExtension);
    } catch (IOException e) {
      throw new StorageException("Error in put.", e);
    }
  }

  @Override
  public final Record get(final Object i) {
    throw new UnsupportedOperationException("File not found");
  }

  @Override
  public final Record get(final Request request) {
    throw new UnsupportedOperationException("File not found");
  }

  @Override
  public final void close() {
    // no closing is required
  }

}
