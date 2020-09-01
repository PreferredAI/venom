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
import org.apache.http.Header;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

public class StorageRecordTest {

  @Test
  void test() {
    final String path = "/test-headers";
    final String headerKey = "Cookie";
    final String headerValue = "text=json;";
    final String url = "http://127.0.0.1/" + path;
    final Map<String, String> headerMap = Collections.singletonMap(headerKey, headerValue);
    final Request request = new VRequest(url, headerMap);

    final Object id = new Object();
    final int statusCode = 200;
    final byte[] content = "This is a test".getBytes();
    final ContentType contentType = ContentType.TEXT_PLAIN;
    final Header[] headers = {new BasicHeader(headerKey, headerValue)};
    final long dateCreated = 0;
    final String md5 = "md5";
    final Map<String, String> body = Collections.singletonMap("body", "body");

    final StorageRecord<Object> storageRecord = StorageRecord.builder(id)
        .setResponseHeaders(headers)
        .setContentType(contentType)
        .setResponseContent(content)
        .setStatusCode(statusCode)
        .setRequestMethod(request.getMethod())
        .setUrl(url)
        .setDateCreated(dateCreated)
        .setMD5(md5)
        .setRequestHeaders(headerMap)
        .setRequestBody(body)
        .build();

    Assertions.assertEquals(id, storageRecord.getId());
    Assertions.assertEquals(headers, storageRecord.getResponseHeaders());
    Assertions.assertEquals(contentType, storageRecord.getContentType());
    Assertions.assertEquals(content, storageRecord.getResponseContent());
    Assertions.assertEquals(statusCode, storageRecord.getStatusCode());
    Assertions.assertEquals(request.getMethod(), storageRecord.getRequestMethod());
    Assertions.assertEquals(url, storageRecord.getURL());
    Assertions.assertEquals(dateCreated, storageRecord.getDateCreated());
    Assertions.assertEquals(md5, storageRecord.getMD5());
    Assertions.assertEquals(headerMap, storageRecord.getRequestHeaders());
    Assertions.assertEquals(body, storageRecord.getRequestBody());
  }

}
