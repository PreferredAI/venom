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

import ai.preferred.venom.request.Request;
import ai.preferred.venom.request.VRequest;
import ai.preferred.venom.response.BaseResponse;
import ai.preferred.venom.response.Response;
import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.util.SocketUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MysqlFileManagerTest {

  private DB db;
  private FileManager fileManager;
  private Path storage;

  @BeforeAll
  void setUp() throws ManagedProcessException, IOException {
    final int randomPort = SocketUtils.findAvailableTcpPort();
    db = DB.newEmbeddedDB(randomPort);
    db.start();
    storage = Files.createTempDirectory("test_storage_directory");
    fileManager = new MysqlFileManager("jdbc:mysql://localhost:" + randomPort + "/test",
        "test", "root", "", storage.toFile());
  }

  @AfterAll
  void tearDown() throws Exception {
    Exception cached = null;
    try {
      fileManager.close();
    } catch (final Exception e) {
      cached = e;
    }

    try {
      Files.walk(storage)
          .sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .forEach(File::delete);
    } catch (final IOException e) {
      if (cached != null) {
        cached.addSuppressed(e);
      } else {
        cached = e;
      }
    }

    try {
      db.stop();
    } catch (final ManagedProcessException e) {
      if (cached != null) {
        cached.addSuppressed(e);
      }
    }

    if (cached != null) {
      throw cached;
    }
  }

  @Test
  void testPut() throws StorageException {
    final int statusCode = 200;
    final String url = "https://preferred.ai/";
    final byte[] content = "This is test data.".getBytes();
    final ContentType contentType = ContentType.create("text/html", StandardCharsets.UTF_8);
    final Header[] headers = {};
    final HttpHost proxy = null;

    final Request request = new VRequest(url);
    final Response response = new BaseResponse(statusCode, url, content, contentType, headers, proxy);

    fileManager.put(request, response);
  }

//  @Test
//  void testGet() throws StorageException {
//    final String url = "https://preferred.ai/";
//    final Request request = new VRequest(url);
//    fileManager.get(request);
//  }
}
