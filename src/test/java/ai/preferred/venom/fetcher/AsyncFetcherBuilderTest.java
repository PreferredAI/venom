/*
 * Copyright (c) 2019 Preferred.AI
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

import ai.preferred.venom.UrlRouter;
import ai.preferred.venom.request.Request;
import ai.preferred.venom.response.Response;
import ai.preferred.venom.storage.FileManager;
import ai.preferred.venom.storage.Record;
import ai.preferred.venom.validator.Validator;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.validation.constraints.NotNull;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;

public class AsyncFetcherBuilderTest {

  @Test
  void testDisableCompression() {
    AsyncFetcher.builder().disableCompression();
  }

  @Test
  void testDisableCookies() {
    AsyncFetcher.builder().disableCookies();
  }

  @Test
  void testRegister() {
    Assertions.assertThrows(IllegalStateException.class, () -> AsyncFetcher.builder().register(null));
    AsyncFetcher.builder().register(new Callback() {
      @Override
      public void completed(@NotNull Request request, @NotNull Response response) {

      }

      @Override
      public void failed(@NotNull Request request, @NotNull Exception ex) {

      }

      @Override
      public void cancelled(@NotNull Request request) {

      }
    });
  }


  @Test
  void testSetRedirectStrategy() {
    AsyncFetcher.builder().setRedirectStrategy(DefaultRedirectStrategy.INSTANCE);
    AsyncFetcher.builder().setRedirectStrategy(null);
  }

  @Test
  void testSetConnectionRequestTimeout() {
    Assertions.assertThrows(IllegalStateException.class, () -> AsyncFetcher.builder().setConnectionRequestTimeout(-2));
    AsyncFetcher.builder().setConnectionRequestTimeout(-1);
    AsyncFetcher.builder().setConnectionRequestTimeout(0);
  }

  @Test
  void testSetConnectTimeout() {
    Assertions.assertThrows(IllegalStateException.class, () -> AsyncFetcher.builder().setConnectTimeout(-2));
    AsyncFetcher.builder().setConnectTimeout(-1);
    AsyncFetcher.builder().setConnectTimeout(0);
  }

  @Test
  void testSetFileManager() {
    AsyncFetcher.builder().setFileManager(null);
    AsyncFetcher.builder().setFileManager(new FileManager<>() {
      @Override
      public @NotNull Callback getCallback() {
        return null;
      }

      @Override
      public @NotNull String put(@NotNull Request request, @NotNull Response response) {
        return null;
      }

      @Nullable
      @Override
      public Record<Object> get(Object id) {
        return null;
      }

      @Override
      public @NotNull Record<Object> get(@NotNull Request request) {
        return null;
      }

      @Override
      public void close() {

      }
    });
  }

  @Test
  void testSetHeaders() {
    Assertions.assertThrows(IllegalStateException.class, () -> AsyncFetcher.builder().setHeaders(null));
    AsyncFetcher.builder().setHeaders(Collections.emptyMap());
  }

  @Test
  void testSetMaxConnections() {
    Assertions.assertThrows(IllegalStateException.class, () -> AsyncFetcher.builder().setMaxConnections(-1));
    Assertions.assertThrows(IllegalStateException.class, () -> AsyncFetcher.builder().setMaxConnections(0));
    AsyncFetcher.builder().setMaxConnections(1);
  }

  @Test
  void testSetMaxRouteConnections() {
    Assertions.assertThrows(IllegalStateException.class, () -> AsyncFetcher.builder().setMaxRouteConnections(-1));
    Assertions.assertThrows(IllegalStateException.class, () -> AsyncFetcher.builder().setMaxRouteConnections(0));
    AsyncFetcher.builder().setMaxRouteConnections(1);
  }

  @Test
  void testSetNumIoThreads() {
    Assertions.assertThrows(IllegalStateException.class, () -> AsyncFetcher.builder().setNumIoThreads(0));
    Assertions.assertThrows(IllegalStateException.class, () -> AsyncFetcher.builder().setNumIoThreads(-1));
    AsyncFetcher.builder().setNumIoThreads(1);
  }

  @Test
  void testSetProxyProvider() {
    AsyncFetcher.builder().setProxyProvider(null);
    AsyncFetcher.builder().setProxyProvider(request -> null);
  }

  @Test
  void testSetSocketTimeout() {
    Assertions.assertThrows(IllegalStateException.class, () -> AsyncFetcher.builder().setSocketTimeout(-2));
    AsyncFetcher.builder().setSocketTimeout(-1);
    AsyncFetcher.builder().setSocketTimeout(0);
  }

  @Test
  void testSetSslContext() throws NoSuchAlgorithmException {
    AsyncFetcher.builder().setSslContext(SSLContext.getDefault());
    AsyncFetcher.builder().setSslContext(null);
  }

  @Test
  void testSetStopCodes() {
    Assertions.assertThrows(IllegalStateException.class, () -> AsyncFetcher.builder().setStopCodes((int[]) null));
    AsyncFetcher.builder().setStopCodes(500);
  }

  @Test
  void testSetThreadFactory() {
    Assertions.assertThrows(IllegalStateException.class, () -> AsyncFetcher.builder().setThreadFactory(null));
    AsyncFetcher.builder().setThreadFactory(r -> null);
  }

  @Test
  void testSetValidator() {
    AsyncFetcher.builder().setValidator(Validator.ALWAYS_VALID);
  }

  @Test
  void testSetValidatorRouter() {
    AsyncFetcher.builder().setValidatorRouter(null);
    AsyncFetcher.builder().setValidatorRouter(new UrlRouter());
  }

  @Test
  void testSetUserAgent() {
    Assertions.assertThrows(IllegalStateException.class, () -> AsyncFetcher.builder().setUserAgent(null));
    AsyncFetcher.builder().setUserAgent(() -> "");
  }

}
