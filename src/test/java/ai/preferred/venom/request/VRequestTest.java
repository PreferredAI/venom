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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

public class VRequestTest {

  @Test
  public void testGetRequest() {
    final String url = "https://venom.preferred.ai";
    final VRequest vRequest = new VRequest(url);
    Assertions.assertEquals(Request.Method.GET, vRequest.getMethod());
    Assertions.assertEquals(url, vRequest.getUrl());
    Assertions.assertTrue(vRequest.getHeaders().isEmpty());
    Assertions.assertNull(vRequest.getBody());
    Assertions.assertNull(vRequest.getProxy());
    Assertions.assertNull(vRequest.getSleepScheduler());
  }

  @Test
  public void testBuilderConstructorRequest() {
    final Request.Method method = Request.Method.DELETE;
    final String url = "https://venom.preferred.ai";
    final VRequest vRequest = VRequest.build(method, url).build();
    Assertions.assertEquals(method, vRequest.getMethod());
    Assertions.assertEquals(url, vRequest.getUrl());
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

    Assertions.assertEquals(Request.Method.POST, vRequest.getMethod());
    Assertions.assertEquals(url, vRequest.getUrl());

    Assertions.assertTrue(vRequest.getHeaders().containsKey(headerKey1));
    Assertions.assertEquals(headerValue1, vRequest.getHeaders().get(headerKey1));

    Assertions.assertTrue(vRequest.getHeaders().containsKey(headerKey2));
    Assertions.assertEquals(headerValue2, vRequest.getHeaders().get(headerKey2));

    Assertions.assertTrue(vRequest.getHeaders().containsKey(headerKey1));
    Assertions.assertEquals(headerValue1, vRequest.getHeaders().get(headerKey1));

    Assertions.assertEquals(body, vRequest.getBody());
    Assertions.assertEquals(proxy, vRequest.getProxy());
    Assertions.assertEquals(sleepScheduler, vRequest.getSleepScheduler());
  }

  @Test
  public void testRemoveHeaderBuilderRequest() {
    final String url = "https://venom.preferred.ai";

    final String headerKey1 = "head1";
    final String headerValue1 = "tail1";

    final String headerKey2 = "head2";
    final String headerValue2 = "tail2";

    final Map<String, String> headers = Collections.singletonMap(headerKey2, headerValue2);

    final VRequest vRequest = VRequest.Builder.get(url)
        .addHeader(headerKey1, headerValue1)
        .addHeaders(headers)
        .removeHeader(headerKey1)
        .build();

    Assertions.assertEquals(Request.Method.GET, vRequest.getMethod());
    Assertions.assertEquals(url, vRequest.getUrl());

    Assertions.assertFalse(vRequest.getHeaders().containsKey(headerKey1));
    Assertions.assertTrue(vRequest.getHeaders().containsKey(headerKey2));
    Assertions.assertEquals(headerValue2, vRequest.getHeaders().get(headerKey2));

    Assertions.assertNull(vRequest.getBody());
    Assertions.assertNull(vRequest.getProxy());
    Assertions.assertNull(vRequest.getSleepScheduler());
  }

  @Test
  public void testRemoveHeadersBuilderRequest() {
    final String url = "https://venom.preferred.ai";

    final String headerKey1 = "head1";
    final String headerValue1 = "tail1";

    final String headerKey2 = "head2";
    final String headerValue2 = "tail2";

    final Map<String, String> headers = Collections.singletonMap(headerKey2, headerValue2);

    final VRequest vRequest = VRequest.Builder.get(url)
        .addHeader(headerKey1, headerValue1)
        .addHeaders(headers)
        .removeHeaders()
        .build();

    Assertions.assertEquals(Request.Method.GET, vRequest.getMethod());
    Assertions.assertTrue(vRequest.getHeaders().isEmpty());
    Assertions.assertNull(vRequest.getBody());
    Assertions.assertNull(vRequest.getProxy());
    Assertions.assertNull(vRequest.getSleepScheduler());
  }

  @Test
  public void testBuilderGetRequest() {
    final String url = "https://venom.preferred.ai";
    final VRequest vRequest = VRequest.Builder.get(url).build();
    Assertions.assertEquals(Request.Method.GET, vRequest.getMethod());
    Assertions.assertEquals(url, vRequest.getUrl());
    Assertions.assertTrue(vRequest.getHeaders().isEmpty());
    Assertions.assertNull(vRequest.getBody());
    Assertions.assertNull(vRequest.getProxy());
    Assertions.assertNull(vRequest.getSleepScheduler());
  }

  @Test
  public void testBuilderPostRequest() {
    final String url = "https://venom.preferred.ai";
    final String body = "BODY";
    final String headerKey = "Content-Type";
    final String headerValue = "application/x-www-form-urlencoded";
    final VRequest vRequest = VRequest.Builder.post(url)
        .setBody(body)
        .addHeader(headerKey, headerValue)
        .build();
    Assertions.assertEquals(Request.Method.POST, vRequest.getMethod());
    Assertions.assertEquals(url, vRequest.getUrl());
    Assertions.assertEquals(headerValue, vRequest.getHeaders().get(headerKey));
    Assertions.assertEquals(body, vRequest.getBody());
    Assertions.assertNull(vRequest.getProxy());
    Assertions.assertNull(vRequest.getSleepScheduler());
  }

  @Test
  public void testBuilderDeleteRequest() {
    final String url = "https://venom.preferred.ai";
    final VRequest vRequest = VRequest.Builder.delete(url).build();
    Assertions.assertEquals(Request.Method.DELETE, vRequest.getMethod());
    Assertions.assertEquals(url, vRequest.getUrl());
    Assertions.assertTrue(vRequest.getHeaders().isEmpty());
    Assertions.assertNull(vRequest.getBody());
    Assertions.assertNull(vRequest.getProxy());
    Assertions.assertNull(vRequest.getSleepScheduler());
  }

  @Test
  public void testBuilderHeadRequest() {
    final String url = "https://venom.preferred.ai";
    final VRequest vRequest = VRequest.Builder.head(url).build();
    Assertions.assertEquals(Request.Method.HEAD, vRequest.getMethod());
    Assertions.assertEquals(url, vRequest.getUrl());
    Assertions.assertTrue(vRequest.getHeaders().isEmpty());
    Assertions.assertNull(vRequest.getBody());
    Assertions.assertNull(vRequest.getProxy());
    Assertions.assertNull(vRequest.getSleepScheduler());
  }

  @Test
  public void testBuilderOptionsRequest() {
    final String url = "https://venom.preferred.ai";
    final VRequest vRequest = VRequest.Builder.options(url).build();
    Assertions.assertEquals(Request.Method.OPTIONS, vRequest.getMethod());
    Assertions.assertEquals(url, vRequest.getUrl());
    Assertions.assertTrue(vRequest.getHeaders().isEmpty());
    Assertions.assertNull(vRequest.getBody());
    Assertions.assertNull(vRequest.getProxy());
    Assertions.assertNull(vRequest.getSleepScheduler());
  }

  @Test
  public void testBuilderPutRequest() {
    final String url = "https://venom.preferred.ai";
    final VRequest vRequest = VRequest.Builder.put(url).build();
    Assertions.assertEquals(Request.Method.PUT, vRequest.getMethod());
    Assertions.assertEquals(url, vRequest.getUrl());
    Assertions.assertTrue(vRequest.getHeaders().isEmpty());
    Assertions.assertNull(vRequest.getBody());
    Assertions.assertNull(vRequest.getProxy());
    Assertions.assertNull(vRequest.getSleepScheduler());
  }

}
