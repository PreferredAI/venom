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

package ai.preferred.venom;

import ai.preferred.venom.fetcher.FakeFetcher;
import ai.preferred.venom.fetcher.Fetcher;
import ai.preferred.venom.job.FIFOScheduler;
import ai.preferred.venom.job.LazyScheduler;
import ai.preferred.venom.request.Request;
import ai.preferred.venom.request.VRequest;
import org.apache.http.HttpHost;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CrawlerTest {

  private final String url = "https://venom.preferred.ai";
  private final VRequest vRequest = new VRequest(url);
  private final Handler handler = (request, response, schedulerH, session, worker) -> {
  };

  @Test
  public void testCrawler() throws Exception {
    final LinkedList<FakeFetcher.Status> statuses = new LinkedList<>();
    statuses.add(FakeFetcher.Status.COMPLETE);
    statuses.add(FakeFetcher.Status.COMPLETE);
    statuses.add(FakeFetcher.Status.COMPLETE);

    final FakeFetcher fetcher = new FakeFetcher(statuses);
    final Handler assertHandler = (request, response, schedulerH, session, worker) -> {
      try {
        Assertions.assertNull(request.getProxy());
        Assertions.assertEquals(url, request.getUrl());
      } catch (AssertionFailedError e) {
        throw new FatalHandlerException(e);
      }
    };

    try (final Crawler crawler = Crawler.builder()
        .setFetcher(fetcher)
        .setMaxConnections(1)
        .setMaxTries(2)
        .setScheduler(new FIFOScheduler())
        .setSleepScheduler(new SleepScheduler(0))
        .build()
        .start()) {

      crawler.getScheduler().add(vRequest, assertHandler);
      crawler.getScheduler().add(vRequest, assertHandler);
      crawler.getScheduler().add(vRequest, assertHandler);
    }

    Assertions.assertEquals(3, fetcher.getCounter());
  }

  @Test
  public void testCrawlerStartAndClose() throws Exception {
    final LinkedList<FakeFetcher.Status> statuses = new LinkedList<>();
    statuses.add(FakeFetcher.Status.COMPLETE);
    statuses.add(FakeFetcher.Status.COMPLETE);
    statuses.add(FakeFetcher.Status.COMPLETE);

    final FakeFetcher fetcher = new FakeFetcher(statuses);

    final Crawler crawler = Crawler.builder()
        .setFetcher(fetcher)
        .setMaxConnections(1)
        .setMaxTries(2)
        .setScheduler(new FIFOScheduler())
        .setSleepScheduler(new SleepScheduler(0))
        .build();

    crawler.getScheduler().add(vRequest, handler);
    crawler.getScheduler().add(vRequest, handler);
    crawler.getScheduler().add(vRequest, handler);

    crawler.startAndClose();

    Assertions.assertEquals(3, fetcher.getCounter());
  }

  @Test
  public void testRetry() throws Exception {
    final LinkedList<FakeFetcher.Status> statuses = new LinkedList<>();
    statuses.add(FakeFetcher.Status.FAILED);
    statuses.add(FakeFetcher.Status.FAILED);
    statuses.add(FakeFetcher.Status.COMPLETE);
    statuses.add(FakeFetcher.Status.COMPLETE);
    statuses.add(FakeFetcher.Status.COMPLETE);

    final FakeFetcher fetcher = new FakeFetcher(statuses);

    try (final Crawler crawler = Crawler.builder()
        .setFetcher(fetcher)
        .setMaxConnections(1)
        .setMaxTries(5)
        .setScheduler(new FIFOScheduler())
        .setSleepScheduler(new SleepScheduler(0))
        .build()
        .start()) {

      crawler.getScheduler().add(vRequest, handler);
    }

    Assertions.assertEquals(3, fetcher.getCounter());
  }

  @Test
  public void testMaxTries() throws Exception {
    final LinkedList<FakeFetcher.Status> statuses = new LinkedList<>();
    statuses.add(FakeFetcher.Status.FAILED);
    statuses.add(FakeFetcher.Status.FAILED);
    statuses.add(FakeFetcher.Status.FAILED);
    statuses.add(FakeFetcher.Status.FAILED);
    statuses.add(FakeFetcher.Status.FAILED);
    statuses.add(FakeFetcher.Status.FAILED);
    statuses.add(FakeFetcher.Status.FAILED);

    final FakeFetcher fetcher = new FakeFetcher(statuses);

    try (final Crawler crawler = Crawler.builder()
        .setFetcher(fetcher)
        .setMaxConnections(1)
        .setMaxTries(5)
        .setScheduler(new FIFOScheduler())
        .setSleepScheduler(new SleepScheduler(0))
        .build()
        .start()) {

      crawler.getScheduler().add(vRequest, handler);
    }

    Assertions.assertEquals(5, fetcher.getCounter());
  }

  @Test
  public void testProxyProportionRemoved() throws Exception {
    final LinkedList<FakeFetcher.Status> statuses = new LinkedList<>();
    statuses.add(FakeFetcher.Status.FAILED);
    statuses.add(FakeFetcher.Status.COMPLETE);

    final HttpHost proxy = new HttpHost("127.0.0.1:8080");
    final FakeFetcher fetcher = new FakeFetcher(statuses);
    final Handler assertHandler = (request, response, schedulerH, session, worker) -> {
      try {
        Assertions.assertEquals(url, request.getUrl());
        Assertions.assertNull(response.getProxy());
      } catch (AssertionFailedError e) {
        throw new FatalHandlerException(e);
      }
    };

    try (final Crawler crawler = Crawler.builder()
        .setFetcher(fetcher)
        .setMaxConnections(1)
        .setPropRetainProxy(0.2)
        .setMaxTries(5)
        .setScheduler(new FIFOScheduler())
        .setSleepScheduler(new SleepScheduler(0))
        .build()
        .start()) {

      final VRequest vRequestProxied = VRequest.Builder.get(url).setProxy(proxy).build();
      crawler.getScheduler().add(vRequestProxied, assertHandler);
    }

    Assertions.assertEquals(2, fetcher.getCounter());
  }

  @Test
  public void testProxyProportionRetained() throws Exception {
    final LinkedList<FakeFetcher.Status> statuses = new LinkedList<>();
    statuses.add(FakeFetcher.Status.COMPLETE);

    final HttpHost proxy = new HttpHost("127.0.0.1:8080");
    final FakeFetcher fetcher = new FakeFetcher(statuses);
    final Handler assertHandler = (request, response, schedulerH, session, worker) -> {
      try {
        Assertions.assertEquals(url, request.getUrl());
        Assertions.assertEquals(proxy, response.getProxy());
      } catch (AssertionFailedError e) {
        throw new FatalHandlerException(e);
      }
    };

    try (final Crawler crawler = Crawler.builder()
        .setFetcher(fetcher)
        .setMaxConnections(1)
        .setPropRetainProxy(0.2)
        .setMaxTries(5)
        .setScheduler(new FIFOScheduler())
        .setSleepScheduler(new SleepScheduler(0))
        .build()
        .start()) {

      final VRequest vRequestProxied = VRequest.Builder.get(url).setProxy(proxy).build();
      crawler.getScheduler().add(vRequestProxied, assertHandler);
    }

    Assertions.assertEquals(1, fetcher.getCounter());
  }

  @Test
  public void testLazySchedulerIntegration() throws Exception {
    final LinkedList<FakeFetcher.Status> statuses = new LinkedList<>();
    statuses.add(FakeFetcher.Status.COMPLETE);
    statuses.add(FakeFetcher.Status.COMPLETE);
    statuses.add(FakeFetcher.Status.COMPLETE);
    statuses.add(FakeFetcher.Status.COMPLETE);

    final FakeFetcher fetcher = new FakeFetcher(statuses);

    final List<Request> requests = new LinkedList<>();
    requests.add(vRequest);
    requests.add(vRequest);
    requests.add(vRequest);

    try (final Crawler crawler = Crawler.builder()
        .setFetcher(fetcher)
        .setMaxConnections(1)
        .setPropRetainProxy(0.2)
        .setMaxTries(5)
        .setScheduler(new LazyScheduler(requests.iterator(), handler))
        .setSleepScheduler(new SleepScheduler(0))
        .build()
        .start()) {

      crawler.getScheduler().add(vRequest, handler);
    }

    Assertions.assertEquals(4, fetcher.getCounter());
  }

  @Test
  public void testUrlRouterIntegration() throws Exception {
    final LinkedList<FakeFetcher.Status> statuses = new LinkedList<>();
    statuses.add(FakeFetcher.Status.COMPLETE);

    final FakeFetcher fetcher = new FakeFetcher(statuses);

    final UrlRouter urlRouter = new UrlRouter(handler);
    try (final Crawler crawler = Crawler.builder()
        .setFetcher(fetcher)
        .setMaxTries(1)
        .setScheduler(new FIFOScheduler())
        .setSleepScheduler(new SleepScheduler(0))
        .setHandlerRouter(urlRouter)
        .build()
        .start()) {

      crawler.getScheduler().add(vRequest);
    }

    Assertions.assertEquals(1, fetcher.getCounter());
  }

  @Test
  public void testFatalHandlerException() {
    Assertions.assertThrows(FatalHandlerException.class, () -> {
      final List<FakeFetcher.Status> statuses = Arrays.asList(FakeFetcher.Status.COMPLETE, FakeFetcher.Status.COMPLETE,
          FakeFetcher.Status.COMPLETE);
      final Fetcher fetcher = new FakeFetcher(new LinkedList<>(statuses));
      try (final Crawler crawler = Crawler.builder()
          .setFetcher(fetcher)
          .setMaxTries(1)
          .setScheduler(new FIFOScheduler())
          .setSleepScheduler(new SleepScheduler(0))
          .build()
          .start()) {

        crawler.getScheduler().add(vRequest, (request, response, scheduler, session, worker) -> {
          // do nothing
        });

        crawler.getScheduler().add(vRequest, (request, response, scheduler, session, worker) -> {
          throw new FatalHandlerException("FatalHandlerException #1");
        });

        crawler.getScheduler().add(vRequest, (request, response, scheduler, session, worker) -> {
          throw new FatalHandlerException("FatalHandlerException #2");
        });
      }
    });
  }

  @Test
  public void testSessionIntegration() throws Exception {
    final LinkedList<FakeFetcher.Status> statuses = new LinkedList<>();
    statuses.add(FakeFetcher.Status.COMPLETE);

    final FakeFetcher fetcher = new FakeFetcher(statuses);

    final Session session = Session.EMPTY_SESSION;
    final Handler assertHandler = (request, response, schedulerH, handleSession, worker) -> {
      try {
        Assertions.assertEquals(session, handleSession);
      } catch (AssertionFailedError e) {
        throw new FatalHandlerException(e);
      }
    };

    try (final Crawler crawler = Crawler.builder()
        .setFetcher(fetcher)
        .setMaxTries(1)
        .setScheduler(new FIFOScheduler())
        .setSleepScheduler(new SleepScheduler(0))
        .setSession(session)
        .build()
        .start()) {

      crawler.getScheduler().add(vRequest, assertHandler);
    }

    Assertions.assertEquals(1, fetcher.getCounter());
  }


}
