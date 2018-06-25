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

import ai.preferred.venom.fetcher.TestFetcher;
import ai.preferred.venom.job.FIFOScheduler;
import ai.preferred.venom.request.VRequest;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;

public class CrawlerTest {

  private final String url = "https://venom.preferred.ai";
  private final VRequest vRequest = new VRequest(url);
  private final Handler handler = (request, response, schedulerH, session, worker) -> {
  };

  @Test
  public void testCrawler() throws Exception {
    final LinkedList<TestFetcher.Status> statuses = new LinkedList<>();
    statuses.add(TestFetcher.Status.COMPLETE);
    statuses.add(TestFetcher.Status.COMPLETE);

    final TestFetcher fetcher = new TestFetcher(statuses);


    try (final Crawler crawler = Crawler.builder()
        .fetcher(fetcher)
        .maxConnections(1)
        .maxTries(2)
        .scheduler(new FIFOScheduler())
        .sleepScheduler(new SleepScheduler(0))
        .build()
        .start()) {

      crawler.getScheduler().add(vRequest, handler);
    }

    Assert.assertEquals(1, fetcher.getCounter());
  }

  @Test
  public void testRetry() throws Exception {
    final LinkedList<TestFetcher.Status> statuses = new LinkedList<>();
    statuses.add(TestFetcher.Status.FAILED);
    statuses.add(TestFetcher.Status.FAILED);
    statuses.add(TestFetcher.Status.COMPLETE);
    statuses.add(TestFetcher.Status.COMPLETE);
    statuses.add(TestFetcher.Status.COMPLETE);

    final TestFetcher fetcher = new TestFetcher(statuses);

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

    Assert.assertEquals(3, fetcher.getCounter());
  }

  @Test
  public void testMaxTries() throws Exception {
    final LinkedList<TestFetcher.Status> statuses = new LinkedList<>();
    statuses.add(TestFetcher.Status.FAILED);
    statuses.add(TestFetcher.Status.FAILED);
    statuses.add(TestFetcher.Status.FAILED);
    statuses.add(TestFetcher.Status.FAILED);
    statuses.add(TestFetcher.Status.FAILED);
    statuses.add(TestFetcher.Status.FAILED);
    statuses.add(TestFetcher.Status.FAILED);

    final TestFetcher fetcher = new TestFetcher(statuses);

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

    Assert.assertEquals(5, fetcher.getCounter());
  }


}
