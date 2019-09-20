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

package ai.preferred.venom;

import ai.preferred.venom.fetcher.AsyncFetcher;
import ai.preferred.venom.job.FIFOJobQueue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CrawlerBuilderTest {

  @Test
  void testSetFetcher() {
    Assertions.assertThrows(IllegalStateException.class, () -> Crawler.builder().setFetcher(null));
    Crawler.builder().setFetcher(AsyncFetcher.buildDefault());
  }

  @Test
  void testSetMaxConnection() {
    Assertions.assertThrows(IllegalStateException.class, () -> Crawler.builder().setMaxConnections(0));
    Assertions.assertThrows(IllegalStateException.class, () -> Crawler.builder().setMaxConnections(-1));
    Crawler.builder().setMaxConnections(1);
  }

  @Test
  void testSetHandlerRouter() {
    Crawler.builder().setHandlerRouter(null);
    Crawler.builder().setHandlerRouter(new UrlRouter());
  }

  @Test
  void testSetMaxTries() {
    Assertions.assertThrows(IllegalStateException.class, () -> Crawler.builder().setMaxTries(0));
    Assertions.assertThrows(IllegalStateException.class, () -> Crawler.builder().setMaxTries(-1));
    Crawler.builder().setMaxTries(1);
  }

  @Test
  void testSetName() {
    Assertions.assertThrows(IllegalStateException.class, () -> Crawler.builder().setName(null));
    Crawler.builder().setName("name");
  }

  @Test
  void testSetParallelism() {
    Assertions.assertThrows(IllegalStateException.class, () -> Crawler.builder().setParallelism(0));
    Assertions.assertThrows(IllegalStateException.class, () -> Crawler.builder().setParallelism(-1));
    Crawler.builder().setParallelism(1);
  }

  @Test
  void testSetPropRetainProxy() {
    Assertions.assertThrows(IllegalStateException.class, () -> Crawler.builder().setPropRetainProxy(2));
    Assertions.assertThrows(IllegalStateException.class, () -> Crawler.builder().setPropRetainProxy(-1));
    Crawler.builder().setPropRetainProxy(.05);
  }

  @Test
  void testSetScheduler() {
    Assertions.assertThrows(IllegalStateException.class, () -> Crawler.builder().setJobQueue(null));
    Crawler.builder().setJobQueue(new FIFOJobQueue());
  }

  @Test
  void testSetSession() {
    Crawler.builder().setSession(null);
    Crawler.builder().setSession(Session.EMPTY_SESSION);
  }

  @Test
  void testSetSleepScheduler() {
    Crawler.builder().setSleepScheduler(null);
    Crawler.builder().setSleepScheduler(new SleepScheduler(1));
  }

  @Test
  void testSetWorkerManager() {
    Assertions.assertThrows(IllegalStateException.class, () -> Crawler.builder().setWorkerManager(null));
    Crawler.builder().setWorkerManager(new ThreadedWorkerManager());
  }

}
