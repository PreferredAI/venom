package ai.preferred.venom.request;

import ai.preferred.venom.SleepScheduler;
import com.google.common.collect.ImmutableMap;
import org.apache.http.HttpHost;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class HttpFetcherRequestTest {

  @Test
  public void testRequest() {
    final String url = "https://venom.preferred.ai";
    final VRequest vRequest = new VRequest(url);
    final HttpFetcherRequest httpFetcherRequest = new HttpFetcherRequest(vRequest);

    Assertions.assertEquals(Request.Method.GET, httpFetcherRequest.getMethod());
    Assertions.assertEquals(url, httpFetcherRequest.getUrl());
    Assertions.assertTrue(httpFetcherRequest.getHeaders().isEmpty());
    Assertions.assertNull(httpFetcherRequest.getBody());
    Assertions.assertNull(httpFetcherRequest.getProxy());
    Assertions.assertNull(httpFetcherRequest.getSleepScheduler());
    Assertions.assertEquals(vRequest, httpFetcherRequest.getInner());
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

    final HttpFetcherRequest httpFetcherRequest = new HttpFetcherRequest(vRequest);

    Assertions.assertEquals(Request.Method.POST, httpFetcherRequest.getMethod());
    Assertions.assertEquals(url, httpFetcherRequest.getUrl());

    Assertions.assertTrue(httpFetcherRequest.getHeaders().containsKey(headerKey1));
    Assertions.assertEquals(headerValue1, httpFetcherRequest.getHeaders().get(headerKey1));

    Assertions.assertTrue(httpFetcherRequest.getHeaders().containsKey(headerKey1));
    Assertions.assertEquals(headerValue1, httpFetcherRequest.getHeaders().get(headerKey1));

    Assertions.assertEquals(body, httpFetcherRequest.getBody());
    Assertions.assertEquals(proxy, httpFetcherRequest.getProxy());
    Assertions.assertEquals(sleepScheduler, httpFetcherRequest.getSleepScheduler());

    Assertions.assertEquals(vRequest, httpFetcherRequest.getInner());

    final String headerKey2 = "head2";
    final String headerValue2 = "tail2";
    final String headerKey3 = "head1";
    final String headerValue3 = "tail3";

    final Map<String, String> headers = ImmutableMap.<String, String>builder()
        .put(headerKey2, headerValue2)
        .put(headerKey3, headerValue3)
        .build();

    final HttpFetcherRequest httpFetcherRequest1 = httpFetcherRequest.prependHeaders(headers);
    Assertions.assertTrue(httpFetcherRequest1.getHeaders().containsKey(headerKey2));
    Assertions.assertEquals(headerValue2, httpFetcherRequest1.getHeaders().get(headerKey2));

    Assertions.assertTrue(httpFetcherRequest1.getHeaders().containsKey(headerKey3));
    Assertions.assertEquals(headerValue1, httpFetcherRequest1.getHeaders().get(headerKey3));
  }

  @Test
  public void testSetProxyRequest() {
    final String url = "https://venom.preferred.ai";
    final VRequest vRequest = new VRequest(url);
    final HttpFetcherRequest httpFetcherRequest = new HttpFetcherRequest(vRequest);

    Assertions.assertEquals(Request.Method.GET, httpFetcherRequest.getMethod());
    Assertions.assertEquals(url, httpFetcherRequest.getUrl());
    Assertions.assertTrue(httpFetcherRequest.getHeaders().isEmpty());
    Assertions.assertNull(httpFetcherRequest.getBody());
    Assertions.assertNull(httpFetcherRequest.getProxy());
    Assertions.assertNull(httpFetcherRequest.getSleepScheduler());
    Assertions.assertEquals(vRequest, httpFetcherRequest.getInner());

    final HttpHost proxy = new HttpHost("127.0.0.1", 80);
    final HttpFetcherRequest httpFetcherRequest1 = httpFetcherRequest.setProxy(proxy);
    Assertions.assertEquals(proxy, httpFetcherRequest1.getProxy());
  }

  @Test
  public void testDiagnosticRequest() {
    final String url = "https://venom.preferred.ai";
    final VRequest vRequest = new VRequest(url);
    final HttpFetcherRequest httpFetcherRequest = new HttpFetcherRequest(vRequest);
    final HttpFetcherRequest.Diagnostics diagnostics = httpFetcherRequest.getDiagnostics();

    Assertions.assertNull(diagnostics.getStart());
    Assertions.assertNull(diagnostics.getAcknowledge());
    Assertions.assertNull(diagnostics.getLatency());
    Assertions.assertNull(diagnostics.getComplete());
    Assertions.assertNull(diagnostics.getSize());
    Assertions.assertNull(diagnostics.getSpeed());

    Assertions.assertFalse(diagnostics.isStarted());
    Assertions.assertFalse(diagnostics.isAcknowledged());
    Assertions.assertFalse(diagnostics.isCompleted());

    diagnostics.setStart();
    Assertions.assertNotNull(diagnostics.getStart());
    Assertions.assertNull(diagnostics.getAcknowledge());
    Assertions.assertNull(diagnostics.getLatency());
    Assertions.assertNull(diagnostics.getComplete());
    Assertions.assertNull(diagnostics.getSize());
    Assertions.assertNull(diagnostics.getSpeed());

    Assertions.assertTrue(diagnostics.isStarted());
    Assertions.assertFalse(diagnostics.isAcknowledged());
    Assertions.assertFalse(diagnostics.isCompleted());

    diagnostics.setAcknowledge();
    Assertions.assertNotNull(diagnostics.getStart());
    Assertions.assertNotNull(diagnostics.getAcknowledge());
    Assertions.assertNotNull(diagnostics.getLatency());
    Assertions.assertNull(diagnostics.getComplete());
    Assertions.assertNull(diagnostics.getSize());
    Assertions.assertNull(diagnostics.getSpeed());

    Assertions.assertTrue(diagnostics.isStarted());
    Assertions.assertTrue(diagnostics.isAcknowledged());
    Assertions.assertFalse(diagnostics.isCompleted());

    diagnostics.setSize(100);
    diagnostics.setComplete();
    Assertions.assertNotNull(diagnostics.getStart());
    Assertions.assertNotNull(diagnostics.getAcknowledge());
    Assertions.assertNotNull(diagnostics.getLatency());
    Assertions.assertNotNull(diagnostics.getComplete());
    Assertions.assertNotNull(diagnostics.getSize());
    Assertions.assertNotNull(diagnostics.getSpeed());

    Assertions.assertTrue(diagnostics.isStarted());
    Assertions.assertTrue(diagnostics.isAcknowledged());
    Assertions.assertTrue(diagnostics.isCompleted());

  }

}
