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

package ai.preferred.venom.job;

import ai.preferred.venom.Handler;
import ai.preferred.venom.request.Request;
import ai.preferred.venom.request.VRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

class LazyQueueSchedulerTest {

  private final String url = "https://venom.preferred.ai";
  private final VRequest vRequest = new VRequest(url);
  private final Job job = new Job(vRequest);

  private LazyQueueScheduler scheduler;

  @BeforeEach
  void initEach() {
    scheduler = new LazyQueueScheduler(null);
  }

  @Test
  void testAddRequest() {
    scheduler.add(job);
    final Job pollJob = scheduler.poll();
    Assertions.assertNotNull(pollJob);
    Assertions.assertEquals(job, pollJob);
    Assertions.assertNotNull(pollJob.getJobAttribute(PriorityJobAttribute.class));
  }

  @Test
  void testPutRequest() throws InterruptedException {
    scheduler.put(job);
    final Job pollJob = scheduler.poll();
    Assertions.assertNotNull(pollJob);
    Assertions.assertEquals(job, pollJob);
    Assertions.assertNotNull(pollJob.getJobAttribute(PriorityJobAttribute.class));
  }

  @Test
  void testOfferRequest() {
    scheduler.offer(job);
    final Job pollJob = scheduler.poll();
    Assertions.assertNotNull(pollJob);
    Assertions.assertEquals(job, pollJob);
    Assertions.assertNotNull(pollJob.getJobAttribute(PriorityJobAttribute.class));
  }

  @Test
  void testOfferTimeoutRequest() throws InterruptedException {
    scheduler.offer(job, 1L, TimeUnit.NANOSECONDS);
    final Job pollJob = scheduler.poll();
    Assertions.assertNotNull(pollJob);
    Assertions.assertEquals(job, pollJob);
    Assertions.assertNotNull(pollJob.getJobAttribute(PriorityJobAttribute.class));
  }

  @Test
  void testPollTimeout() throws InterruptedException {
    scheduler.add(job);
    final Job pollJob = scheduler.poll(1L, TimeUnit.NANOSECONDS);
    Assertions.assertEquals(job, pollJob);
  }

  @Test
  void testIterator() {
    final List<Request> requests = new ArrayList<>();
    final VRequest vRequestNeg = new VRequest(url);

    requests.add(vRequest);
    requests.add(vRequestNeg);
    requests.add(vRequestNeg);
    requests.add(vRequestNeg);

    final Handler handler = (request, response, schedulerH, session, worker) -> {

    };

    final LazyQueueScheduler scheduler = new LazyQueueScheduler(requests.iterator(), handler);

    final Job pollJob = scheduler.poll();
    Assertions.assertNotNull(pollJob);
    Assertions.assertEquals(vRequest, pollJob.getRequest());
    Assertions.assertEquals(handler, pollJob.getHandler());
    Assertions.assertNotNull(pollJob.getJobAttribute(PriorityJobAttribute.class));
    Assertions.assertEquals(
        Priority.DEFAULT,
        pollJob.getJobAttribute(PriorityJobAttribute.class).getPriority()
    );
  }

  @Test
  void testLazyQueue() {
    final List<Request> requests = new ArrayList<>();
    final VRequest vRequestNeg = new VRequest(url);

    requests.add(vRequestNeg);
    requests.add(vRequestNeg);
    requests.add(vRequestNeg);
    requests.add(vRequestNeg);

    final LazyQueueScheduler scheduler = new LazyQueueScheduler(requests.iterator());
    final Job job = new Job(vRequest);

    scheduler.add(job);
    final Job pollJob = scheduler.poll();
    Assertions.assertEquals(job, pollJob);
  }

  @Test
  void testIsEmpty() {
    final List<Request> requests = new ArrayList<>();
    requests.add(vRequest);
    final LazyQueueScheduler scheduler = new LazyQueueScheduler(requests.iterator());

    scheduler.add(job);
    Assertions.assertFalse(scheduler.isEmpty());
    scheduler.poll();
    Assertions.assertFalse(scheduler.isEmpty());
    scheduler.poll();
    Assertions.assertTrue(scheduler.isEmpty());
    scheduler.add(job);
    Assertions.assertFalse(scheduler.isEmpty());
    scheduler.poll();
    Assertions.assertTrue(scheduler.isEmpty());
  }

}
