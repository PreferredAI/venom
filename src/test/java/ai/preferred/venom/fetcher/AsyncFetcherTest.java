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
import ai.preferred.venom.response.VResponse;
import ai.preferred.venom.storage.FakeFileManager;
import ai.preferred.venom.storage.FileManager;
import ai.preferred.venom.storage.Record;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.common.collect.ImmutableList;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.constraints.NotNull;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPInputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class AsyncFetcherTest {

  private final byte[] content;
  private Fetcher fetcher;
  private WireMockServer wireMockServer;

  public AsyncFetcherTest() throws IOException {
    final InputStream stream = getClass().getClassLoader().getResourceAsStream("venom.html.gz");
    Assertions.assertNotNull(stream);
    content = IOUtils.toByteArray(
        new BufferedInputStream(
            new GZIPInputStream(stream)
        )
    );
  }

  @BeforeEach
  public void init() {
    fetcher = AsyncFetcher.buildDefault();
    fetcher.start();
    wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
    wireMockServer.start();
  }

  @AfterEach
  public void close() throws Exception {
    fetcher.close();
    wireMockServer.stop();
  }

  @Test
  public void testGet() throws ExecutionException, InterruptedException {
    final int port = wireMockServer.port();
    configureFor("localhost", port);
    final String path = "/test-fetch";
    stubFor(get(urlEqualTo(path))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/html; charset=utf-8")
            .withBody(content)));

    final Request request = new VRequest("http://127.0.0.1:" + port + path);
    final Future<Response> responseFuture = fetcher.fetch(request);
    final Response response = responseFuture.get();
    Assertions.assertEquals(200, response.getStatusCode());
    Assertions.assertEquals("http://127.0.0.1:" + port + path, response.getUrl());
    Assertions.assertEquals("text/html", response.getContentType().getMimeType());
    Assertions.assertEquals(StandardCharsets.UTF_8, response.getContentType().getCharset());

    final VResponse vResponse = new VResponse(response);
    Assertions.assertTrue(vResponse.getHtml().contains("Venom is an open source focused crawler for the deep web."));
  }

  @Test
  public void testPost() throws ExecutionException, InterruptedException {
    final int port = wireMockServer.port();
    configureFor("localhost", port);
    final String path = "/test-fetch";
    stubFor(post(urlEqualTo(path))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/html; charset=utf-8")
            .withBody(content)));

    final Request request = VRequest.Builder.post("http://127.0.0.1:" + port + path).build();
    final Future<Response> responseFuture = fetcher.fetch(request);
    final Response response = responseFuture.get();
    Assertions.assertEquals(200, response.getStatusCode());
    Assertions.assertEquals("http://127.0.0.1:" + port + path, response.getUrl());
    Assertions.assertEquals("text/html", response.getContentType().getMimeType());
    Assertions.assertEquals(StandardCharsets.UTF_8, response.getContentType().getCharset());

    final VResponse vResponse = new VResponse(response);
    Assertions.assertTrue(vResponse.getHtml().contains("Venom is an open source focused crawler for the deep web."));
  }

//  @Test
//  public void testHead() throws ExecutionException, InterruptedException {
//    final int port = wireMockServer.port();
//    configureFor("localhost", port);
//    final String path = "/test-fetch";
//    stubFor(head(urlEqualTo(path))
//        .willReturn(aResponse()
//            .withStatus(200)
//            .withHeader("Content-Type", "text/html; charset=utf-8")
//            .withBody(content)));
//
//    final Request request = VRequest.Builder.head("http://127.0.0.1:" + port + path).build();
//    final Future<Response> responseFuture = fetcher.fetch(request);
//    final Response response = responseFuture.get();
//    Assertions.assertEquals(200, response.getStatusCode());
//    Assertions.assertEquals("http://127.0.0.1:" + port + path, response.getUrl());
//    Assertions.assertEquals("text/html", response.getContentType().getMimeType());
//    Assertions.assertEquals(StandardCharsets.UTF_8, response.getContentType().getCharset());
//
//    final VResponse vResponse = new VResponse(response);
//    Assertions.assertTrue(vResponse.getHtml().contains("Venom is an open source focused crawler for the deep web."));
//  }

  @Test
  public void testPut() throws ExecutionException, InterruptedException {
    final int port = wireMockServer.port();
    configureFor("localhost", port);
    final String path = "/test-fetch";
    stubFor(put(urlEqualTo(path))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/html; charset=utf-8")
            .withBody(content)));

    final Request request = VRequest.Builder.put("http://127.0.0.1:" + port + path).build();
    final Future<Response> responseFuture = fetcher.fetch(request);
    final Response response = responseFuture.get();
    Assertions.assertEquals(200, response.getStatusCode());
    Assertions.assertEquals("http://127.0.0.1:" + port + path, response.getUrl());
    Assertions.assertEquals("text/html", response.getContentType().getMimeType());
    Assertions.assertEquals(StandardCharsets.UTF_8, response.getContentType().getCharset());

    final VResponse vResponse = new VResponse(response);
    Assertions.assertTrue(vResponse.getHtml().contains("Venom is an open source focused crawler for the deep web."));
  }

  @Test
  public void testDelete() throws ExecutionException, InterruptedException {
    final int port = wireMockServer.port();
    configureFor("localhost", port);
    final String path = "/test-fetch";
    stubFor(delete(urlEqualTo(path))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/html; charset=utf-8")
            .withBody(content)));

    final Request request = VRequest.Builder.delete("http://127.0.0.1:" + port + path).build();
    final Future<Response> responseFuture = fetcher.fetch(request);
    final Response response = responseFuture.get();
    Assertions.assertEquals(200, response.getStatusCode());
    Assertions.assertEquals("http://127.0.0.1:" + port + path, response.getUrl());
    Assertions.assertEquals("text/html", response.getContentType().getMimeType());
    Assertions.assertEquals(StandardCharsets.UTF_8, response.getContentType().getCharset());

    final VResponse vResponse = new VResponse(response);
    Assertions.assertTrue(vResponse.getHtml().contains("Venom is an open source focused crawler for the deep web."));
  }

  @Test
  public void testOptions() throws ExecutionException, InterruptedException {
    final int port = wireMockServer.port();
    configureFor("localhost", port);
    final String path = "/test-fetch";
    stubFor(options(urlEqualTo(path))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/html; charset=utf-8")
            .withBody(content)));

    final Request request = VRequest.Builder.options("http://127.0.0.1:" + port + path).build();
    final Future<Response> responseFuture = fetcher.fetch(request);
    final Response response = responseFuture.get();
    Assertions.assertEquals(200, response.getStatusCode());
    Assertions.assertEquals("http://127.0.0.1:" + port + path, response.getUrl());
    Assertions.assertEquals("text/html", response.getContentType().getMimeType());
    Assertions.assertEquals(StandardCharsets.UTF_8, response.getContentType().getCharset());

    final VResponse vResponse = new VResponse(response);
    Assertions.assertTrue(vResponse.getHtml().contains("Venom is an open source focused crawler for the deep web."));
  }

  @Test
  public void testParseImageContentType() throws ExecutionException, InterruptedException, IOException {
    final InputStream stream = getClass().getClassLoader().getResourceAsStream("venom.png");
    Assertions.assertNotNull(stream);
    final byte[] content = IOUtils.toByteArray(
        new BufferedInputStream(stream)
    );

    final int port = wireMockServer.port();
    configureFor("localhost", port);
    final String path = "/test-content-type";
    stubFor(get(urlEqualTo(path))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "")
            .withBody(content)));

    final Request request = new VRequest("http://127.0.0.1:" + port + path);
    final Future<Response> responseFuture = fetcher.fetch(request);
    final Response response = responseFuture.get();
    Assertions.assertEquals(200, response.getStatusCode());
    Assertions.assertEquals("http://127.0.0.1:" + port + path, response.getUrl());
    Assertions.assertEquals("image/png", response.getContentType().getMimeType());
  }

  @Test
  public void testParseHTMLContentType() throws ExecutionException, InterruptedException {
    final int port = wireMockServer.port();
    configureFor("localhost", port);
    final String path = "/test-charset";
    stubFor(get(urlEqualTo(path))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "")
            .withBody(content)));

    final Request request = new VRequest("http://127.0.0.1:" + port + path);
    final Future<Response> responseFuture = fetcher.fetch(request);
    final Response response = responseFuture.get();
    Assertions.assertEquals(200, response.getStatusCode());
    Assertions.assertEquals("http://127.0.0.1:" + port + path, response.getUrl());
    Assertions.assertEquals("text/html", response.getContentType().getMimeType());

    final VResponse vResponse = new VResponse(response);
    Assertions.assertTrue(vResponse.getHtml().contains("Venom is an open source focused crawler for the deep web."));
  }

  @Test
  public void testParseJSONCharset() throws ExecutionException, InterruptedException, IOException {
    final InputStream stream = getClass().getClassLoader().getResourceAsStream("venom.json");
    Assertions.assertNotNull(stream);
    final byte[] content = IOUtils.toByteArray(
        new BufferedInputStream(stream)
    );

    final int port = wireMockServer.port();
    configureFor("localhost", port);
    final String path = "/test-content-type";
    stubFor(get(urlEqualTo(path))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/json")
            .withBody(content)));


    final Request request = new VRequest("http://127.0.0.1:" + port + path);
    final Future<Response> responseFuture = fetcher.fetch(request);
    final Response response = responseFuture.get();
    Assertions.assertEquals(200, response.getStatusCode());
    Assertions.assertEquals("http://127.0.0.1:" + port + path, response.getUrl());
    Assertions.assertEquals("text/json", response.getContentType().getMimeType());
    Assertions.assertEquals(StandardCharsets.UTF_8, response.getContentType().getCharset());

    final VResponse vResponse = new VResponse(response);
    Assertions.assertTrue(vResponse.getHtml().contains("Venom is an open source focused crawler for the deep web."));
  }

  @Test
  public void testValidator() {
    final int port = wireMockServer.port();
    configureFor("localhost", port);
    final String path = "/test-validator";
    stubFor(get(urlEqualTo(path))
        .willReturn(notFound()));

    final Request request = new VRequest("http://127.0.0.1:" + port + path);
    final Future<Response> responseFuture = fetcher.fetch(request);

    Assertions.assertThrows(ExecutionException.class, responseFuture::get);
  }

  @Test
  public void testUserAgent() throws ExecutionException, InterruptedException {
    final int port = wireMockServer.port();
    configureFor("localhost", port);
    final String path = "/test-user-agent";
    stubFor(get(urlEqualTo(path))
        .withHeader("User-Agent", matching("^Venom.*"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "")
            .withBody(content)));

    final Request request = new VRequest("http://127.0.0.1:" + port + path);
    final Future<Response> responseFuture = fetcher.fetch(request);
    final Response response = responseFuture.get();
    Assertions.assertEquals(200, response.getStatusCode());
    Assertions.assertEquals("http://127.0.0.1:" + port + path, response.getUrl());

    final VResponse vResponse = new VResponse(response);
    Assertions.assertTrue(vResponse.getHtml().contains("Venom is an open source focused crawler for the deep web."));
  }

  @Test
  public void testReplaceUserAgent() throws ExecutionException, InterruptedException {
    final int port = wireMockServer.port();
    configureFor("localhost", port);
    final String path = "/test-replace-user-agent";
    final String headerKey = "User-Agent";
    final String headerValue = "Not Venom";
    stubFor(get(urlEqualTo(path))
        .withHeader("User-Agent", notMatching("^Venom.*"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBody(content)));


    final Map<String, String> headers = Collections.singletonMap(headerKey, headerValue);
    final Request request = new VRequest("http://127.0.0.1:" + port + path, headers);
    final Future<Response> responseFuture = fetcher.fetch(request);
    final Response response = responseFuture.get();
    Assertions.assertEquals(200, response.getStatusCode());
    Assertions.assertEquals("http://127.0.0.1:" + port + path, response.getUrl());

    final VResponse vResponse = new VResponse(response);
    Assertions.assertTrue(vResponse.getHtml().contains("Venom is an open source focused crawler for the deep web."));
  }

  @Test
  public void testHeaders() throws ExecutionException, InterruptedException {
    final int port = wireMockServer.port();
    configureFor("localhost", port);
    final String path = "/test-headers";
    final String headerKey = "Content-Type";
    final String headerValue = "text/json";
    stubFor(get(urlEqualTo(path))
        .withHeader("Content-Type", equalTo("text/json"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "")
            .withBody(content)));


    final Map<String, String> headers = Collections.singletonMap(headerKey, headerValue);
    final Request request = new VRequest("http://127.0.0.1:" + port + path, headers);
    final Future<Response> responseFuture = fetcher.fetch(request);
    final Response response = responseFuture.get();
    Assertions.assertEquals(200, response.getStatusCode());
    Assertions.assertEquals("http://127.0.0.1:" + port + path, response.getUrl());

    final VResponse vResponse = new VResponse(response);
    Assertions.assertTrue(vResponse.getHtml().contains("Venom is an open source focused crawler for the deep web."));
  }

  @Test
  public void testPresetHeaders() throws Exception {
    fetcher.close();
    final String headerKey = "XHeader";
    final String headerValue = "Venom";
    final Map<String, String> headersPreset = Collections.singletonMap(headerKey, headerValue);
    fetcher = AsyncFetcher.builder().setHeaders(headersPreset).build();
    fetcher.start();

    final int port = wireMockServer.port();
    configureFor("localhost", port);
    final String path = "/test-preset-headers";
    stubFor(get(urlEqualTo(path))
        .withHeader(headerKey, matching("^Venom$"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBody(content)));

    final Request request = new VRequest("http://127.0.0.1:" + port + path);
    final Future<Response> responseFuture = fetcher.fetch(request);
    final Response response = responseFuture.get();
    Assertions.assertEquals(200, response.getStatusCode());
    Assertions.assertEquals("http://127.0.0.1:" + port + path, response.getUrl());

    final VResponse vResponse = new VResponse(response);
    Assertions.assertTrue(vResponse.getHtml().contains("Venom is an open source focused crawler for the deep web."));
  }

  @Test
  public void testPrependHeaders() throws Exception {
    fetcher.close();
    final String headerKey = "XHeader";
    final String headerValue = "Venom";
    final Map<String, String> headersPreset = Collections.singletonMap(headerKey, headerValue);
    fetcher = AsyncFetcher.builder().setHeaders(headersPreset).build();
    fetcher.start();

    final int port = wireMockServer.port();
    configureFor("localhost", port);
    final String path = "/test-prepend";
    stubFor(get(urlEqualTo(path))
        .withHeader(headerKey, notMatching("^Venom$"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBody(content)));

    final String NewHeaderValue = "Not Venom";
    final Map<String, String> headers = Collections.singletonMap(headerKey, NewHeaderValue);
    final Request request = new VRequest("http://127.0.0.1:" + port + path, headers);
    final Future<Response> responseFuture = fetcher.fetch(request);
    final Response response = responseFuture.get();
    Assertions.assertEquals(200, response.getStatusCode());
    Assertions.assertEquals("http://127.0.0.1:" + port + path, response.getUrl());

    final VResponse vResponse = new VResponse(response);
    Assertions.assertTrue(vResponse.getHtml().contains("Venom is an open source focused crawler for the deep web."));
  }

  @Test
  public void testFileMangerIntegration() throws Exception {
    fetcher.close();
    final FileManager<Object> fileManager = new FakeFileManager();
    fetcher = AsyncFetcher.builder().setFileManager(fileManager).build();
    fetcher.start();

    final int port = wireMockServer.port();
    configureFor("localhost", port);
    final String path = "/test-file-manager";
    stubFor(get(urlEqualTo(path))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/html; charset=utf-8")
            .withBody(content)));

    final Request request = new VRequest("http://127.0.0.1:" + port + path);
    final Future<Response> responseFuture = fetcher.fetch(request);
    final Response response = responseFuture.get();
    Assertions.assertEquals(200, response.getStatusCode());
    Assertions.assertEquals("http://127.0.0.1:" + port + path, response.getUrl());
    Assertions.assertEquals("text/html", response.getContentType().getMimeType());
    Assertions.assertEquals(StandardCharsets.UTF_8, response.getContentType().getCharset());

    final VResponse vResponse = new VResponse(response);
    Assertions.assertTrue(vResponse.getHtml().contains("Venom is an open source focused crawler for the deep web."));

    fetcher.close();
    final Record<Object> record = fileManager.get(request);
    Assertions.assertNotNull(record, "Record not found.");
  }

  @Test
  public void testCallbackIntegration() throws Exception {
    fetcher.close();
    final AtomicBoolean completed = new AtomicBoolean(false);
    final Callback callback = new Callback() {
      @Override
      public void completed(@NotNull Request request, @NotNull Response response) {
        completed.set(true);
      }

      @Override
      public void failed(@NotNull Request request, @NotNull Exception ex) {

      }

      @Override
      public void cancelled(@NotNull Request request) {

      }
    };
    fetcher = AsyncFetcher.builder().register(callback).build();
    fetcher.start();

    final int port = wireMockServer.port();
    configureFor("localhost", port);
    final String path = "/test-file-manager";
    stubFor(get(urlEqualTo(path))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/html; charset=utf-8")
            .withBody(content)));

    final Request request = new VRequest("http://127.0.0.1:" + port + path);
    final Future<Response> responseFuture = fetcher.fetch(request);
    final Response response = responseFuture.get();
    Assertions.assertEquals(200, response.getStatusCode());
    Assertions.assertEquals("http://127.0.0.1:" + port + path, response.getUrl());
    Assertions.assertEquals("text/html", response.getContentType().getMimeType());
    Assertions.assertEquals(StandardCharsets.UTF_8, response.getContentType().getCharset());

    final VResponse vResponse = new VResponse(response);
    Assertions.assertTrue(vResponse.getHtml().contains("Venom is an open source focused crawler for the deep web."));

    fetcher.close();
    Assertions.assertTrue(completed.get(), "Callback complete function not called");
  }

  @Test
  public void testStopCode() throws Exception {
    fetcher.close();
    fetcher = AsyncFetcher.builder().setStopCodes(410).build();
    fetcher.start();

    final int port = wireMockServer.port();
    configureFor("localhost", port);
    final String path = "/test-fetch";
    stubFor(get(urlEqualTo(path))
        .willReturn(aResponse()
            .withStatus(410)
            .withHeader("Content-Type", "text/html; charset=utf-8")
            .withBody(content)));

    final Request request = new VRequest("http://127.0.0.1:" + port + path);
    final Future<Response> responseFuture = fetcher.fetch(request);

    final AtomicBoolean thrown = new AtomicBoolean(false);
    try {
      responseFuture.get();
    } catch (InterruptedException | ExecutionException e) {
      Assertions.assertTrue(e.getCause() instanceof StopCodeException);
      thrown.set(true);
    }

    Assertions.assertTrue(thrown.get(), "StopCodeException not thrown.");
  }

  @Test
  public void testClosed() throws Exception {
    fetcher.close();

    final Request request = new VRequest("http://127.0.0.1");
    final Future<Response> responseFuture = fetcher.fetch(request);

    final AtomicBoolean thrown = new AtomicBoolean(false);
    try {
      responseFuture.get();
    } catch (CancellationException e) {
      thrown.set(true);
    } catch (InterruptedException | ExecutionException e) {
      Assertions.fail("Wrong exception");
    }

    Assertions.assertTrue(thrown.get(), "CancellationException not thrown.");
  }

  @Test
  public void testRedirection() throws Exception {
    final int port = wireMockServer.port();
    configureFor("localhost", port);
    final List<String> paths = ImmutableList.of(
        "/test-redirect-1",
        "/test-redirect-2",
        "/test-fetch"
    );

    for (int i = 0; i < paths.size() - 1; i++) {
      stubFor(get(urlEqualTo(paths.get(i)))
          .willReturn(temporaryRedirect(paths.get(i + 1))));
    }

    stubFor(get(urlEqualTo(paths.get(paths.size() - 1)))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/html; charset=utf-8")
            .withBody(content)));

    final Request request = new VRequest("http://127.0.0.1:" + port + paths.get(0));
    final Future<Response> responseFuture = fetcher.fetch(request);
    final Response response = responseFuture.get();
    Assertions.assertEquals(200, response.getStatusCode());
    Assertions.assertEquals("http://127.0.0.1:" + port + paths.get(paths.size() - 1), response.getUrl());
    Assertions.assertEquals("text/html", response.getContentType().getMimeType());
    Assertions.assertEquals(StandardCharsets.UTF_8, response.getContentType().getCharset());

    final VResponse vResponse = new VResponse(response);
    Assertions.assertTrue(vResponse.getHtml().contains("Venom is an open source focused crawler for the deep web."));
  }

}
