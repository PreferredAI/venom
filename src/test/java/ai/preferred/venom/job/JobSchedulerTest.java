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

package ai.preferred.venom.job;

import ai.preferred.venom.Handler;
import ai.preferred.venom.request.VRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JobSchedulerTest {

  private final String url = "https://venom.preferred.ai";
  private final VRequest vRequest = new VRequest(url);
  private final Handler handler = (request, response, scheduler, session, worker) -> {

  };

  @Test
  void testAddRequest() {
    final FIFOQueueScheduler scheduler = new FIFOQueueScheduler();
    scheduler.getScheduler().add(vRequest);
    final Job job = scheduler.poll();
    Assertions.assertNotNull(job);
    Assertions.assertEquals(vRequest, job.getRequest());
    Assertions.assertNull(job.getHandler());
  }

  @Test
  void testAddRequestHandler() {
    final FIFOQueueScheduler scheduler = new FIFOQueueScheduler();
    scheduler.getScheduler().add(vRequest, handler);
    final Job job = scheduler.poll();
    Assertions.assertNotNull(job);
    Assertions.assertEquals(vRequest, job.getRequest());
    Assertions.assertEquals(handler, job.getHandler());
  }

  @Test
  void testAddRequestJobAttribute() {
    final FIFOQueueScheduler scheduler = new FIFOQueueScheduler();
    final PriorityJobAttribute priorityJobAttribute = new PriorityJobAttribute();
    scheduler.getScheduler().add(vRequest, priorityJobAttribute);
    final Job job = scheduler.poll();
    Assertions.assertNotNull(job);
    Assertions.assertEquals(vRequest, job.getRequest());
    Assertions.assertNull(job.getHandler());
    Assertions.assertEquals(priorityJobAttribute, job.getJobAttribute(priorityJobAttribute.getClass()));
  }

  @Test
  void testAddRequestHandlerJobAttribute() {
    final FIFOQueueScheduler scheduler = new FIFOQueueScheduler();
    final PriorityJobAttribute priorityJobAttribute = new PriorityJobAttribute();
    scheduler.getScheduler().add(vRequest, handler, priorityJobAttribute);
    final Job job = scheduler.poll();
    Assertions.assertNotNull(job);
    Assertions.assertEquals(vRequest, job.getRequest());
    Assertions.assertEquals(handler, job.getHandler());
    Assertions.assertEquals(priorityJobAttribute, job.getJobAttribute(priorityJobAttribute.getClass()));
  }

}
