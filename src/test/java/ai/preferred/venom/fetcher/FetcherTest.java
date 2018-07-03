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
import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.zip.GZIPInputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class FetcherTest {

  private Fetcher fetcher;

  private WireMockServer wireMockServer;

  private final byte[] content;

  public FetcherTest() throws IOException {
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
  public void testFetch() throws ExecutionException, InterruptedException {
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
    Assertions.assertEquals("http://127.0.0.1:" + port, response.getBaseUrl());
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
    Assertions.assertEquals("http://127.0.0.1:" + port, response.getBaseUrl());
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
    Assertions.assertEquals("http://127.0.0.1:" + port, response.getBaseUrl());
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
    Assertions.assertEquals("http://127.0.0.1:" + port, response.getBaseUrl());
    Assertions.assertEquals("text/json", response.getContentType().getMimeType());
    Assertions.assertEquals(StandardCharsets.UTF_8, response.getContentType().getCharset());

    final VResponse vResponse = new VResponse(response);
    Assertions.assertTrue(vResponse.getHtml().contains("Venom is an open source focused crawler for the deep web."));
  }

}
