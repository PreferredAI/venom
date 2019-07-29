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

import ai.preferred.venom.request.VRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

class PriorityQueueSchedulerTest {

  private final String url = "https://venom.preferred.ai";
  private final VRequest vRequest = new VRequest(url);
  private final Job job = new Job(vRequest);

  private PriorityQueueScheduler scheduler;

  @BeforeEach
  void initEach() {
    scheduler = new PriorityQueueScheduler();
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
  void testPriorityQueue() {
    final Job job = new Job(vRequest, null, new PriorityJobAttribute(Priority.HIGHEST));
    scheduler.add(new Job(vRequest, null, new PriorityJobAttribute(Priority.HIGH)));
    scheduler.add(job);
    scheduler.add(new Job(vRequest, null, new PriorityJobAttribute(Priority.DEFAULT)));
    scheduler.add(new Job(vRequest, null, new PriorityJobAttribute(Priority.LOW)));

    final Job pollJob = scheduler.poll();
    Assertions.assertEquals(pollJob, job);
  }

}
