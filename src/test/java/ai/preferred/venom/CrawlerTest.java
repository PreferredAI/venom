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
import ai.preferred.venom.job.FIFOScheduler;
import ai.preferred.venom.job.LazyScheduler;
import ai.preferred.venom.request.Request;
import ai.preferred.venom.request.VRequest;
import org.apache.http.HttpHost;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
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
      Assertions.assertEquals(url, request.getUrl());
      Assertions.assertNull(request.getProxy());
    };

    try (final Crawler crawler = Crawler.builder()
        .fetcher(fetcher)
        .maxConnections(1)
        .maxTries(2)
        .scheduler(new FIFOScheduler())
        .sleepScheduler(new SleepScheduler(0))
        .build()
        .start()) {

      crawler.getScheduler().add(vRequest, assertHandler);
      crawler.getScheduler().add(vRequest, assertHandler);
      crawler.getScheduler().add(vRequest, assertHandler);
    }

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
        .fetcher(fetcher)
        .maxConnections(1)
        .maxTries(5)
        .scheduler(new FIFOScheduler())
        .sleepScheduler(new SleepScheduler(0))
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
        .fetcher(fetcher)
        .maxConnections(1)
        .maxTries(5)
        .scheduler(new FIFOScheduler())
        .sleepScheduler(new SleepScheduler(0))
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
      Assertions.assertEquals(url, request.getUrl());
      Assertions.assertNull(response.getProxy());
    };

    try (final Crawler crawler = Crawler.builder()
        .fetcher(fetcher)
        .maxConnections(1)
        .propRetainProxy(0.2)
        .maxTries(5)
        .scheduler(new FIFOScheduler())
        .sleepScheduler(new SleepScheduler(0))
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
      Assertions.assertEquals(url, request.getUrl());
      Assertions.assertEquals(proxy, response.getProxy());
    };

    try (final Crawler crawler = Crawler.builder()
        .fetcher(fetcher)
        .maxConnections(1)
        .propRetainProxy(0.2)
        .maxTries(5)
        .scheduler(new FIFOScheduler())
        .sleepScheduler(new SleepScheduler(0))
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
        .fetcher(fetcher)
        .maxConnections(1)
        .propRetainProxy(0.2)
        .maxTries(5)
        .scheduler(new LazyScheduler(requests.iterator(), handler))
        .sleepScheduler(new SleepScheduler(0))
        .build()
        .start()) {

      final VRequest vRequestProxied = new VRequest(url, Collections.singletonMap("key", ""));
      crawler.getScheduler().add(vRequestProxied, handler);
    }

    Assertions.assertEquals(4, fetcher.getCounter());
  }


}
