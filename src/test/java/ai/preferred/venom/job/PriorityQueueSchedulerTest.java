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
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PriorityQueueSchedulerTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void testAddRequest() {
    final PriorityQueueScheduler scheduler = new PriorityQueueScheduler();

    final String url = "url";
    final VRequest vRequest = new VRequest(url);

    scheduler.add(vRequest);
    Job job = scheduler.poll();
    Assert.assertNotNull(job);
    Assert.assertEquals(vRequest, job.getRequest());
    Assert.assertNull(job.getHandler());
    Assert.assertEquals(Priority.DEFAULT, job.getPriority());
  }

  @Test
  public void testAddRequestHandler() {
    final PriorityQueueScheduler scheduler = new PriorityQueueScheduler();

    final String url = "url";
    final VRequest vRequest = new VRequest(url);

    final Handler handler = (request, response, schedulerH, session, worker) -> {

    };

    scheduler.add(vRequest, handler);
    Job job = scheduler.poll();
    Assert.assertNotNull(job);
    Assert.assertEquals(vRequest, job.getRequest());
    Assert.assertEquals(handler, job.getHandler());
    Assert.assertEquals(Priority.DEFAULT, job.getPriority());
  }

  @Test
  public void testPriority() {
    final PriorityQueueScheduler scheduler = new PriorityQueueScheduler();

    final String url = "url";
    final VRequest vRequest = new VRequest(url);
    final VRequest vRequestNeg = new VRequest(url);

    scheduler.add(vRequestNeg, Priority.HIGH);
    scheduler.add(vRequest, Priority.HIGHEST);
    scheduler.add(vRequestNeg, Priority.DEFAULT);
    scheduler.add(vRequestNeg, Priority.LOW);

    Job job = scheduler.poll();
    Assert.assertNotNull(job);
    Assert.assertEquals(vRequest, job.getRequest());
    Assert.assertNull(job.getHandler());
    Assert.assertEquals(Priority.HIGHEST, job.getPriority());
  }

  @Test
  public void testPriorityFloor() {
    final PriorityQueueScheduler scheduler = new PriorityQueueScheduler();

    final String url = "url";
    final VRequest vRequest = new VRequest(url);

    scheduler.add(vRequest, Priority.HIGH, Priority.NORMAL);

    final Job job = scheduler.poll();
    Assert.assertNotNull(job);
    Assert.assertEquals(vRequest, job.getRequest());
    Assert.assertNull(job.getHandler());
    Assert.assertEquals(Priority.HIGH, job.getPriority());

    job.reQueue();
    final Job jobRQ = scheduler.poll();
    Assert.assertNotNull(jobRQ);
    Assert.assertEquals(vRequest, jobRQ.getRequest());
    Assert.assertNull(jobRQ.getHandler());
    Assert.assertEquals(Priority.NORMAL, jobRQ.getPriority());

    job.reQueue();
    final Job jobRQRQ = scheduler.poll();
    Assert.assertNotNull(jobRQRQ);
    Assert.assertEquals(vRequest, jobRQRQ.getRequest());
    Assert.assertNull(jobRQRQ.getHandler());
    Assert.assertEquals(Priority.NORMAL, jobRQRQ.getPriority());
  }

}
