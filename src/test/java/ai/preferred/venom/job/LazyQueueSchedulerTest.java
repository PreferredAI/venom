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
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class LazyQueueSchedulerTest {

  @Test
  public void testIterator() {
    final List<Request> requests = new ArrayList<>();

    final String url = "https://venom.preferred.ai";
    final VRequest vRequestNeg = new VRequest(url);
    final VRequest vRequest = new VRequest(url);

    requests.add(vRequest);
    requests.add(vRequestNeg);
    requests.add(vRequestNeg);
    requests.add(vRequestNeg);

    final Handler handler = (request, response, schedulerH, session, worker) -> {

    };

    final LazyQueueScheduler scheduler = new LazyQueueScheduler(requests.iterator(), handler);

    final Job job = scheduler.poll();
    Assertions.assertNotNull(job);
    Assertions.assertEquals(vRequest, job.getRequest());
    Assertions.assertEquals(handler, job.getHandler());
    Assertions.assertEquals(Priority.DEFAULT, job.getPriority());
  }

  @Test
  public void testLazyQueue() {
    final List<Request> requests = new ArrayList<>();

    final String url = "https://venom.preferred.ai";
    final VRequest vRequestNeg = new VRequest(url);

    requests.add(vRequestNeg);
    requests.add(vRequestNeg);
    requests.add(vRequestNeg);
    requests.add(vRequestNeg);

    final Handler handler = (request, response, schedulerH, session, worker) -> {

    };


    final LazyQueueScheduler scheduler = new LazyQueueScheduler(requests.iterator(), handler);
    final VRequest vRequest = new VRequest(url);
    scheduler.getScheduler().add(vRequest);

    final Job job = scheduler.poll();
    Assertions.assertNotNull(job);
    Assertions.assertEquals(vRequest, job.getRequest());
    Assertions.assertNull(job.getHandler());
    Assertions.assertEquals(Priority.DEFAULT, job.getPriority());
  }

}
