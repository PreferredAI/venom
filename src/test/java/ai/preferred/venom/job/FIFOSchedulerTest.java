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

public class FIFOSchedulerTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void testAddRequest() {
    final FIFOScheduler scheduler = new FIFOScheduler();

    final String url = "https://venom.preferred.ai";
    final VRequest vRequest = new VRequest(url);

    scheduler.add(vRequest);
    final Job job = scheduler.poll();
    Assert.assertNotNull(job);
    Assert.assertEquals(vRequest, job.getRequest());
    Assert.assertNull(job.getHandler());
    Assert.assertEquals(Priority.DEFAULT, job.getPriority());
  }

  @Test
  public void testAddRequestHandler() {
    final FIFOScheduler scheduler = new FIFOScheduler();

    final String url = "https://venom.preferred.ai";
    final VRequest vRequest = new VRequest(url);

    final Handler handler = (request, response, schedulerH, session, worker) -> {

    };

    scheduler.add(vRequest, handler);
    final Job job = scheduler.poll();
    Assert.assertNotNull(job);
    Assert.assertEquals(vRequest, job.getRequest());
    Assert.assertEquals(handler, job.getHandler());
    Assert.assertEquals(Priority.DEFAULT, job.getPriority());
  }

  @Test
  public void testFIFOQueue() {
    final FIFOScheduler scheduler = new FIFOScheduler();

    final String url = "https://venom.preferred.ai";
    final VRequest vRequest = new VRequest(url);
    final VRequest vRequestNeg = new VRequest(url);

    scheduler.add(vRequest, Priority.HIGH);
    scheduler.add(vRequestNeg, Priority.HIGHEST);
    scheduler.add(vRequestNeg, Priority.DEFAULT);
    scheduler.add(vRequestNeg, Priority.LOW);

    final Job job = scheduler.poll();
    Assert.assertNotNull(job);
    Assert.assertEquals(vRequest, job.getRequest());
    Assert.assertNull(job.getHandler());
    Assert.assertEquals(Priority.HIGH, job.getPriority());
  }

}
