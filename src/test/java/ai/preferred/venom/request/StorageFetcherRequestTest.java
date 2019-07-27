package ai.preferred.venom.request;

import ai.preferred.venom.SleepScheduler;
import com.google.common.collect.ImmutableMap;
import org.apache.http.HttpHost;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class StorageFetcherRequestTest {

  @Test
  public void testRequest() {
    final String url = "https://venom.preferred.ai";
    final VRequest vRequest = new VRequest(url);
    final StorageFetcherRequest storageFetcherRequest = new StorageFetcherRequest(vRequest);

    Assertions.assertEquals(Request.Method.GET, storageFetcherRequest.getMethod());
    Assertions.assertEquals(url, storageFetcherRequest.getUrl());
    Assertions.assertTrue(storageFetcherRequest.getHeaders().isEmpty());
    Assertions.assertNull(storageFetcherRequest.getBody());
    Assertions.assertNull(storageFetcherRequest.getProxy());
    Assertions.assertNull(storageFetcherRequest.getSleepScheduler());
    Assertions.assertEquals(vRequest, storageFetcherRequest.getInner());
  }

  @Test
  public void testPrependHeaderRequest() {
    final HttpHost proxy = new HttpHost("127.0.0.1");
    final SleepScheduler sleepScheduler = new SleepScheduler(0);
    final String body = "body";
    final String url = "https://venom.preferred.ai";

    final String headerKey1 = "head1";
    final String headerValue1 = "tail1";

    final VRequest vRequest = VRequest.Builder.post(url)
        .addHeader(headerKey1, headerValue1)
        .setBody(body)
        .setProxy(proxy)
        .setSleepScheduler(sleepScheduler)
        .build();

    final StorageFetcherRequest storageFetcherRequest = new StorageFetcherRequest(vRequest);

    Assertions.assertEquals(Request.Method.POST, storageFetcherRequest.getMethod());
    Assertions.assertEquals(url, storageFetcherRequest.getUrl());

    Assertions.assertTrue(storageFetcherRequest.getHeaders().containsKey(headerKey1));
    Assertions.assertEquals(headerValue1, storageFetcherRequest.getHeaders().get(headerKey1));

    Assertions.assertTrue(storageFetcherRequest.getHeaders().containsKey(headerKey1));
    Assertions.assertEquals(headerValue1, storageFetcherRequest.getHeaders().get(headerKey1));

    Assertions.assertEquals(body, storageFetcherRequest.getBody());
    Assertions.assertEquals(proxy, storageFetcherRequest.getProxy());
    Assertions.assertEquals(sleepScheduler, storageFetcherRequest.getSleepScheduler());

    Assertions.assertEquals(vRequest, storageFetcherRequest.getInner());

    final String headerKey2 = "head2";
    final String headerValue2 = "tail2";
    final String headerKey3 = "head1";
    final String headerValue3 = "tail3";

    final Map<String, String> headers = ImmutableMap.<String, String>builder()
        .put(headerKey2, headerValue2)
        .put(headerKey3, headerValue3)
        .build();

    final StorageFetcherRequest storageFetcherRequest1 = storageFetcherRequest.prependHeaders(headers);
    Assertions.assertTrue(storageFetcherRequest1.getHeaders().containsKey(headerKey2));
    Assertions.assertEquals(headerValue2, storageFetcherRequest1.getHeaders().get(headerKey2));

    Assertions.assertTrue(storageFetcherRequest.getHeaders().containsKey(headerKey3));
    Assertions.assertEquals(headerValue1, storageFetcherRequest.getHeaders().get(headerKey3));
  }

}
