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
import ai.preferred.venom.request.VRequest;
import ai.preferred.venom.response.BaseResponse;
import ai.preferred.venom.response.Response;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.Header;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DummyFileManagerTest {

  private DummyFileManager fileManager;

  private Path storage;

  @BeforeAll
  void setUp() throws IOException {
    storage = Files.createTempDirectory("test_storage_directory");
    fileManager = new DummyFileManager(storage.toFile());
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
      //noinspection ResultOfMethodCallIgnored
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

    final Request request = new VRequest(url);
    final Response response = new BaseResponse(statusCode, url, content, contentType, headers, null);

    final String md5 = DigestUtils.md5Hex(content);
    final String subDirName = md5.substring(0, 2);

    final String path = fileManager.put(request, response);
    final String expectedPath = new File(new File(storage.toFile(), subDirName), md5).toString() + ".html";
    Assertions.assertEquals(expectedPath, path);
  }

  @Test
  void testGetId() {
    Assertions.assertThrows(UnsupportedOperationException.class, () -> fileManager.get(new Object()));
  }

  @Test
  void testGetRequest() {
    final String url = "https://preferred.ai/";
    final Request request = new VRequest(url);
    Assertions.assertThrows(UnsupportedOperationException.class, () -> fileManager.get(request));
  }
}
