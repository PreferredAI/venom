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
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class FileManagerCallbackTest {

  @Test
  void testCompleted() throws StorageException {
    final String url = "https://preferred.ai/";
    final Request request = new VRequest(url);

    final int statusCode = 200;
    final String baseUrl = request.getUrl();
    final byte[] content = "IPSUM".getBytes();
    final ContentType contentType = ContentType.create("text/html", StandardCharsets.UTF_8);
    final Header[] headers = {};
    final HttpHost proxy = request.getProxy();

    final Response response = new BaseResponse(statusCode, baseUrl, content, contentType, headers, proxy);

    final FileManager<Object> fileManager = new FakeFileManager();
    fileManager.getCallback().completed(request, response);

    final Record<Object> record = fileManager.get(request);
    Assertions.assertNotNull(record);
  }

  @Test
  void testFailed() throws StorageException {
    final String url = "https://preferred.ai/";
    final Request request = new VRequest(url);

    final FileManager<Object> fileManager = new FakeFileManager();
    fileManager.getCallback().failed(request, new StorageException(""));

    final Record<Object> record = fileManager.get(request);
    Assertions.assertNull(record);
  }

  @Test
  void testCancelled() throws StorageException {
    final String url = "https://preferred.ai/";
    final Request request = new VRequest(url);

    final FileManager<Object> fileManager = new FakeFileManager();
    fileManager.getCallback().cancelled(request);

    final Record<Object> record = fileManager.get(request);
    Assertions.assertNull(record);
  }

}
