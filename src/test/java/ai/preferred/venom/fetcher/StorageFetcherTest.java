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

package ai.preferred.venom.fetcher;

import ai.preferred.venom.request.Request;
import ai.preferred.venom.request.VRequest;
import ai.preferred.venom.response.Response;
import ai.preferred.venom.storage.FakeFileManager;
import ai.preferred.venom.storage.Record;
import ai.preferred.venom.storage.StorageException;
import ai.preferred.venom.storage.StorageRecord;
import ai.preferred.venom.validator.Validator;
import org.apache.http.Header;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;


public class StorageFetcherTest {

  @Test
  public void testTrue() throws Exception {
    final String path = "/test-fetch";
    final String url = "http://127.0.0.1/" + path;
    final Request request = new VRequest(url);

    final int statusCode = 200;
    final byte[] content = "This is a test".getBytes();
    final ContentType contentType = ContentType.TEXT_PLAIN;
    final Record record = StorageRecord.builder()
        .setUrl(url)
        .setRequestMethod(request.getMethod())
        .setStatusCode(statusCode)
        .setResponseContent(content)
        .setContentType(contentType)
        .build();

    final FakeFileManager fileManager = new FakeFileManager(Collections.singletonMap(request, record));
    final Validator validator = Validator.ALWAYS_VALID;
    try (final Fetcher fetcher = StorageFetcher.builder(fileManager).setValidator(validator).build()) {
      fetcher.start();
      final Future<Response> responseFuture = fetcher.fetch(request);
      final Response response = responseFuture.get();
      Assertions.assertEquals(statusCode, response.getStatusCode());
      Assertions.assertEquals(content, response.getContent());
      Assertions.assertEquals(contentType, response.getContentType());
      Assertions.assertNull(response.getProxy());
    }

    Assertions.assertTrue(fileManager.getClosed());
  }

  @Test
  public void testHeadersTrue() throws Exception {
    final String path = "/test-headers";
    final String headerKey = "Cookie";
    final String headerValue = "text=json;";
    final String url = "http://127.0.0.1/" + path;
    final Request request = new VRequest(url);

    final int statusCode = 200;
    final byte[] content = "This is a test".getBytes();
    final ContentType contentType = ContentType.TEXT_PLAIN;
    final Header[] headers = {new BasicHeader(headerKey, headerValue)};
    final Record record = StorageRecord.builder()
        .setUrl(url)
        .setRequestMethod(request.getMethod())
        .setStatusCode(statusCode)
        .setResponseContent(content)
        .setContentType(contentType)
        .setResponseHeaders(headers)
        .build();

    final FakeFileManager fileManager = new FakeFileManager(Collections.singletonMap(request, record));
    final Validator validator = Validator.ALWAYS_VALID;
    try (final Fetcher fetcher = StorageFetcher.builder(fileManager).setValidator(validator).build()) {
      fetcher.start();
      final Future<Response> responseFuture = fetcher.fetch(request);
      final Response response = responseFuture.get();
      Assertions.assertEquals(statusCode, response.getStatusCode());
      Assertions.assertEquals(content, response.getContent());
      Assertions.assertEquals(contentType, response.getContentType());
      Assertions.assertEquals(headers, response.getHeaders());
      Assertions.assertNull(response.getProxy());
    }

    Assertions.assertTrue(fileManager.getClosed());
  }

  @Test
  public void testFetcherHeadersTrue() throws Exception {
    final String path = "/fetcher-headers";
    final String headerKey = "Cookie";
    final String headerValue = "text=json;";
    final String url = "http://127.0.0.1/" + path;
    final Request submittedRequest = new VRequest(url);

    final int statusCode = 200;
    final byte[] content = "This is a test".getBytes();
    final ContentType contentType = ContentType.TEXT_PLAIN;

    final Header[] headers = {new BasicHeader(headerKey, headerValue)};
    final Record record = StorageRecord.builder()
        .setUrl(url)
        .setRequestMethod(submittedRequest.getMethod())
        .setStatusCode(statusCode)
        .setResponseContent(content)
        .setContentType(contentType)
        .setResponseHeaders(headers)
        .build();

    final Map<String, String> headerMap = Collections.singletonMap(headerKey, headerValue);
    final Request request = new VRequest(url, headerMap);
    final FakeFileManager fileManager = new FakeFileManager(Collections.singletonMap(request, record));
    final Validator validator = Validator.ALWAYS_VALID;
    try (final Fetcher fetcher = StorageFetcher.builder(fileManager).setValidator(validator)
        .setHeaders(headerMap).build()) {
      final Future<Response> responseFuture = fetcher.fetch(submittedRequest);
      final Response response = responseFuture.get();
      Assertions.assertEquals(statusCode, response.getStatusCode());
      Assertions.assertEquals(content, response.getContent());
      Assertions.assertEquals(contentType, response.getContentType());
      Assertions.assertEquals(headers, response.getHeaders());
      Assertions.assertNull(response.getProxy());
    }

    Assertions.assertTrue(fileManager.getClosed());
  }

  @Test
  public void testNotFound() throws Exception {
    final String path = "/not-found";
    final String headerKey = "Cookie";
    final String headerValue = "text=json;";
    final String url = "http://127.0.0.1/" + path;
    final Request request = new VRequest(url);

    final int statusCode = 200;
    final byte[] content = "This is a test".getBytes();
    final ContentType contentType = ContentType.TEXT_PLAIN;

    final Header[] headers = {new BasicHeader(headerKey, headerValue)};
    final Record record = StorageRecord.builder()
        .setUrl(url)
        .setRequestMethod(request.getMethod())
        .setStatusCode(statusCode)
        .setResponseContent(content)
        .setContentType(contentType)
        .setResponseHeaders(headers)
        .build();

    final Map<String, String> headerMap = Collections.singletonMap(headerKey, headerValue);
    final FakeFileManager fileManager = new FakeFileManager(Collections.singletonMap(request, record));
    final Validator validator = Validator.ALWAYS_VALID;
    final AtomicBoolean thrown = new AtomicBoolean(false);
    try (final Fetcher fetcher = StorageFetcher.builder(fileManager).setValidator(validator)
        .setHeaders(headerMap).build()) {
      final Future<Response> responseFuture = fetcher.fetch(request);
      Assertions.assertTrue(responseFuture.isCancelled());
      try {
        responseFuture.get();
      } catch (CancellationException e) {
        thrown.set(true);
      } catch (InterruptedException | ExecutionException e) {
        Assertions.fail("Wrong exception");
      }
    }

    Assertions.assertTrue(thrown.get(), "CancellationException not thrown.");
    Assertions.assertTrue(fileManager.getClosed());
  }

  @Test
  public void testFailure() throws Exception {
    final String path = "/test-failure";
    final String url = "http://127.0.0.1/" + path;
    final Request request = new VRequest(url);

    final int statusCode = 200;
    final byte[] content = "This is a test".getBytes();
    final ContentType contentType = ContentType.TEXT_PLAIN;
    final Record record = StorageRecord.builder()
        .setUrl(url)
        .setRequestMethod(request.getMethod())
        .setStatusCode(statusCode)
        .setResponseContent(content)
        .setContentType(contentType)
        .build();

    final FakeFileManager fileManager = new FakeFileManager(Collections.singletonMap(null, record));
    final Validator validator = Validator.ALWAYS_VALID;
    final AtomicBoolean thrown = new AtomicBoolean(false);
    try (final Fetcher fetcher = StorageFetcher.builder(fileManager).setValidator(validator).build()) {
      final Future<Response> responseFuture = fetcher.fetch(request);
      try {
        responseFuture.get();
      } catch (InterruptedException | ExecutionException e) {
        Assertions.assertTrue(e.getCause() instanceof StorageException);
        thrown.set(true);
      }
    }

    Assertions.assertTrue(thrown.get(), "StorageException not thrown.");
    Assertions.assertTrue(fileManager.getClosed());
  }

  @Test
  public void testValidation() throws Exception {
    final String path = "/test-validation";
    final String url = "http://127.0.0.1/" + path;
    final Request request = new VRequest(url);

    final int statusCode = 500;
    final byte[] content = "This is a test".getBytes();
    final ContentType contentType = ContentType.TEXT_PLAIN;
    final Record record = StorageRecord.builder()
        .setUrl(url)
        .setRequestMethod(request.getMethod())
        .setStatusCode(statusCode)
        .setResponseContent(content)
        .setContentType(contentType)
        .build();

    final FakeFileManager fileManager = new FakeFileManager(Collections.singletonMap(request, record));
    final AtomicBoolean thrown = new AtomicBoolean(false);
    try (final Fetcher fetcher = StorageFetcher.builder(fileManager).build()) {
      final Future<Response> responseFuture = fetcher.fetch(request);
      try {
        final Response response = responseFuture.get();
      } catch (InterruptedException | ExecutionException e) {
        Assertions.assertTrue(e.getCause() instanceof ValidationException);
        thrown.set(true);
      }
    }

    Assertions.assertTrue(thrown.get(), "ValidationException not thrown.");
    Assertions.assertTrue(fileManager.getClosed());
  }
}