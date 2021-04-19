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

class JobTest {


  @Test
  void testJob() {
    final String url = "https://venom.preferred.ai";
    final VRequest vRequest = new VRequest(url);
    final Handler handler = new Handler() {
      @Override
      public void tokenize() {

      }

      @Override
      public void parse() {

      }

      @Override
      public void extract() {

      }
    };

    final FakeJobAttribute fakeJobAttribute = new FakeJobAttribute();

    final Job job = new Job(vRequest, handler, fakeJobAttribute);
    Assertions.assertEquals(vRequest, job.getRequest());
    Assertions.assertEquals(handler, job.getHandler());

    final JobAttribute jobAttribute = job.getJobAttribute(fakeJobAttribute.getClass());
    Assertions.assertEquals(fakeJobAttribute, jobAttribute);
  }

  @Test
  void testJobTryCount() {
    final String url = "https://venom.preferred.ai";
    final VRequest vRequest = new VRequest(url);

    final Job job = new Job(vRequest, null);
    Assertions.assertEquals(1, job.getTryCount());

    job.prepareRetry();
    Assertions.assertEquals(2, job.getTryCount());
  }

  @Test
  void testJobPrepareRetry() {
    final String url = "https://venom.preferred.ai";
    final VRequest vRequest = new VRequest(url);

    final FakeJobAttribute fakeJobAttribute = new FakeJobAttribute();
    final Job job = new Job(vRequest, null, fakeJobAttribute);
    Assertions.assertEquals(0, fakeJobAttribute.getCount());

    job.prepareRetry();
    Assertions.assertEquals(1, fakeJobAttribute.getCount());
  }

  @Test
  void testAddJobAttributes() {
    final String url = "https://venom.preferred.ai";
    final VRequest vRequest = new VRequest(url);

    final Job job = new Job(vRequest, null);

    final FakeJobAttribute fakeJobAttribute = new FakeJobAttribute();

    job.setJobAttribute(fakeJobAttribute);
    final JobAttribute jobAttribute = job.getJobAttribute(fakeJobAttribute.getClass());
    Assertions.assertEquals(fakeJobAttribute, jobAttribute);
  }

}
