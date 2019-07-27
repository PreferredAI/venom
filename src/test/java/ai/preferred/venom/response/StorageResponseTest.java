package ai.preferred.venom.response;

import ai.preferred.venom.request.Request;
import ai.preferred.venom.request.VRequest;
import ai.preferred.venom.storage.StorageRecord;
import org.apache.http.Header;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

public class StorageResponseTest {

  @Test
  public void testStorageResponse() {
    final String path = "/test-response";
    final String headerKey = "Cookie";
    final String headerValue = "text=json;";
    final String url = "http://127.0.0.1/" + path;
    final Map<String, String> headerMap = Collections.singletonMap(headerKey, headerValue);
    final Request request = new VRequest(url, headerMap);

    final int statusCode = 200;
    final byte[] content = "This is a test".getBytes();
    final ContentType contentType = ContentType.TEXT_PLAIN;
    final Header[] headers = {new BasicHeader(headerKey, headerValue)};
    final Map<String, String> body = Collections.singletonMap("body", "body");

    final StorageRecord storageRecord = StorageRecord.builder()
        .setResponseHeaders(headers)
        .setContentType(contentType)
        .setResponseContent(content)
        .setStatusCode(statusCode)
        .setRequestMethod(request.getMethod())
        .setUrl(url)
        .setRequestHeaders(headerMap)
        .setRequestBody(body)
        .build();

    final StorageResponse storageResponse = new StorageResponse(storageRecord, url);

    Assertions.assertEquals(statusCode, storageResponse.getStatusCode());
    Assertions.assertEquals(content, storageResponse.getContent());
    Assertions.assertEquals(contentType, storageResponse.getContentType());
    Assertions.assertEquals(headers, storageResponse.getHeaders());
    Assertions.assertEquals(url, storageResponse.getBaseUrl());
    Assertions.assertNull(storageResponse.getProxy());
    Assertions.assertEquals(storageRecord, storageResponse.getRecord());
  }

}
