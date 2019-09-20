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

class PriorityJobQueueTest {

  private final String url = "https://venom.preferred.ai";
  private final VRequest vRequest = new VRequest(url);
  private final Job job = new Job(vRequest);

  private PriorityJobQueue jobQueue;

  @BeforeEach
  void initEach() {
    jobQueue = new PriorityJobQueue();
  }

  @Test
  void testAddRequest() {
    jobQueue.add(job);
    final Job pollJob = jobQueue.poll();
    Assertions.assertNotNull(pollJob);
    Assertions.assertEquals(job, pollJob);
    Assertions.assertNotNull(pollJob.getJobAttribute(PriorityJobAttribute.class));
  }

  @Test
  void testPutRequest() throws InterruptedException {
    jobQueue.put(job);
    final Job pollJob = jobQueue.poll();
    Assertions.assertNotNull(pollJob);
    Assertions.assertEquals(job, pollJob);
    Assertions.assertNotNull(pollJob.getJobAttribute(PriorityJobAttribute.class));
  }

  @Test
  void testOfferRequest() {
    jobQueue.offer(job);
    final Job pollJob = jobQueue.poll();
    Assertions.assertNotNull(pollJob);
    Assertions.assertEquals(job, pollJob);
    Assertions.assertNotNull(pollJob.getJobAttribute(PriorityJobAttribute.class));
  }

  @Test
  void testOfferTimeoutRequest() throws InterruptedException {
    jobQueue.offer(job, 1L, TimeUnit.NANOSECONDS);
    final Job pollJob = jobQueue.poll();
    Assertions.assertNotNull(pollJob);
    Assertions.assertEquals(job, pollJob);
    Assertions.assertNotNull(pollJob.getJobAttribute(PriorityJobAttribute.class));
  }

  @Test
  void testPollTimeout() throws InterruptedException {
    jobQueue.add(job);
    final Job pollJob = jobQueue.poll(1L, TimeUnit.NANOSECONDS);
    Assertions.assertEquals(job, pollJob);
  }

  @Test
  void testPriorityQueue() {
    final Job job = new Job(vRequest, null, new PriorityJobAttribute(Priority.HIGHEST));
    jobQueue.add(new Job(vRequest, null, new PriorityJobAttribute(Priority.HIGH)));
    jobQueue.add(job);
    jobQueue.add(new Job(vRequest, null, new PriorityJobAttribute(Priority.DEFAULT)));
    jobQueue.add(new Job(vRequest, null, new PriorityJobAttribute(Priority.LOW)));

    final Job pollJob = jobQueue.poll();
    Assertions.assertEquals(pollJob, job);
  }

}
