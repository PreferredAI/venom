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
import ai.preferred.venom.request.VRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PriorityQueueSchedulerTest {

  @Test
  public void testAddRequest() {
    final PriorityQueueScheduler scheduler = new PriorityQueueScheduler();

    final String url = "https://venom.preferred.ai";
    final VRequest vRequest = new VRequest(url);

    scheduler.getScheduler().add(vRequest);
    final Job job = scheduler.poll();
    Assertions.assertNotNull(job);
    Assertions.assertEquals(vRequest, job.getRequest());
    Assertions.assertNull(job.getHandler());
    Assertions.assertEquals(Priority.DEFAULT, job.getPriority());
  }

  @Test
  public void testAddRequestHandler() {
    final PriorityQueueScheduler scheduler = new PriorityQueueScheduler();

    final String url = "https://venom.preferred.ai";
    final VRequest vRequest = new VRequest(url);

    final Handler handler = (request, response, schedulerH, session, worker) -> {

    };

    scheduler.getScheduler().add(vRequest, handler);
    final Job job = scheduler.poll();
    Assertions.assertNotNull(job);
    Assertions.assertEquals(vRequest, job.getRequest());
    Assertions.assertEquals(handler, job.getHandler());
    Assertions.assertEquals(Priority.DEFAULT, job.getPriority());
  }

  @Test
  public void testPriority() {
    final PriorityQueueScheduler scheduler = new PriorityQueueScheduler();

    final String url = "https://venom.preferred.ai";
    final VRequest vRequest = new VRequest(url);
    final VRequest vRequestNeg = new VRequest(url);

    scheduler.getScheduler().add(vRequestNeg, Priority.HIGH);
    scheduler.getScheduler().add(vRequest, Priority.HIGHEST);
    scheduler.getScheduler().add(vRequestNeg, Priority.DEFAULT);
    scheduler.getScheduler().add(vRequestNeg, Priority.LOW);

    final Job job = scheduler.poll();
    Assertions.assertNotNull(job);
    Assertions.assertEquals(vRequest, job.getRequest());
    Assertions.assertNull(job.getHandler());
    Assertions.assertEquals(Priority.HIGHEST, job.getPriority());
  }

  @Test
  public void testPriorityFloor() {
    final PriorityQueueScheduler scheduler = new PriorityQueueScheduler();

    final String url = "https://venom.preferred.ai";
    final VRequest vRequest = new VRequest(url);

    scheduler.getScheduler().add(vRequest, Priority.HIGH, Priority.NORMAL);

    final Job job = scheduler.poll();
    Assertions.assertNotNull(job);
    Assertions.assertEquals(vRequest, job.getRequest());
    Assertions.assertNull(job.getHandler());
    Assertions.assertEquals(Priority.HIGH, job.getPriority());

    job.reQueue();
    final Job jobRQ = scheduler.poll();
    Assertions.assertNotNull(jobRQ);
    Assertions.assertEquals(vRequest, jobRQ.getRequest());
    Assertions.assertNull(jobRQ.getHandler());
    Assertions.assertEquals(Priority.NORMAL, jobRQ.getPriority());

    job.reQueue();
    final Job jobRQRQ = scheduler.poll();
    Assertions.assertNotNull(jobRQRQ);
    Assertions.assertEquals(vRequest, jobRQRQ.getRequest());
    Assertions.assertNull(jobRQRQ.getHandler());
    Assertions.assertEquals(Priority.NORMAL, jobRQRQ.getPriority());
  }

}
