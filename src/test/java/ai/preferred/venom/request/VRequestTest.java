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

package ai.preferred.venom.request;

import ai.preferred.venom.SleepScheduler;
import org.apache.http.HttpHost;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

public class VRequestTest {

  @Test
  public void testGetRequest() {
    final String url = "https://venom.preferred.ai";
    final VRequest vRequest = new VRequest(url);
    Assert.assertEquals(Request.Method.GET, vRequest.getMethod());
    Assert.assertEquals(url, vRequest.getUrl());
    Assert.assertTrue(vRequest.getHeaders().isEmpty());
    Assert.assertNull(vRequest.getBody());
    Assert.assertNull(vRequest.getProxy());
    Assert.assertNull(vRequest.getSleepScheduler());
  }

  @Test
  public void testBuilderConstructorRequest() {
    final Request.Method method = Request.Method.DELETE;
    final String url = "https://venom.preferred.ai";
    final VRequest vRequest = VRequest.build(method, url).build();
    Assert.assertEquals(method, vRequest.getMethod());
    Assert.assertEquals(url, vRequest.getUrl());
  }

  @Test
  public void testBuilderRequest() {
    final HttpHost proxy = new HttpHost("127.0.0.1");
    final SleepScheduler sleepScheduler = new SleepScheduler(0);
    final String body = "body";
    final String url = "https://venom.preferred.ai";

    final String headerKey1 = "head1";
    final String headerValue1 = "tail1";

    final String headerKey2 = "head2";
    final String headerValue2 = "tail2";

    final Map<String, String> headers = Collections.singletonMap(headerKey2, headerValue2);

    final VRequest vRequest = VRequest.Builder.post(url)
        .addHeader(headerKey1, headerValue1)
        .addHeaders(headers)
        .setBody(body)
        .setProxy(proxy)
        .setSleepScheduler(sleepScheduler)
        .build();

    Assert.assertEquals(Request.Method.POST, vRequest.getMethod());
    Assert.assertEquals(url, vRequest.getUrl());

    Assert.assertTrue(vRequest.getHeaders().containsKey(headerKey1));
    Assert.assertEquals(headerValue1, vRequest.getHeaders().get(headerKey1));

    Assert.assertTrue(vRequest.getHeaders().containsKey(headerKey2));
    Assert.assertEquals(headerValue2, vRequest.getHeaders().get(headerKey2));

    Assert.assertTrue(vRequest.getHeaders().containsKey(headerKey1));
    Assert.assertEquals(headerValue1, vRequest.getHeaders().get(headerKey1));

    Assert.assertEquals(body, vRequest.getBody());
    Assert.assertEquals(proxy, vRequest.getProxy());
    Assert.assertEquals(sleepScheduler, vRequest.getSleepScheduler());
  }

}
