package ai.preferred.venom.request;

import ai.preferred.venom.SleepScheduler;
import org.apache.http.HttpHost;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CrawlerRequestTest {

  @Test
  public void testRequest() {
    final String url = "https://venom.preferred.ai";
    final VRequest vRequest = new VRequest(url);
    final CrawlerRequest crawlerRequest = new CrawlerRequest(vRequest);

    Assertions.assertEquals(Request.Method.GET, crawlerRequest.getMethod());
    Assertions.assertEquals(url, crawlerRequest.getUrl());
    Assertions.assertTrue(crawlerRequest.getHeaders().isEmpty());
    Assertions.assertNull(crawlerRequest.getBody());
    Assertions.assertNull(crawlerRequest.getProxy());
    Assertions.assertNull(crawlerRequest.getSleepScheduler());
    Assertions.assertEquals(vRequest, crawlerRequest.getInner());
  }

  @Test
  public void testRemoveProxyRequest() {
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

    final CrawlerRequest crawlerRequest = new CrawlerRequest(vRequest);

    Assertions.assertEquals(Request.Method.POST, crawlerRequest.getMethod());
    Assertions.assertEquals(url, crawlerRequest.getUrl());

    Assertions.assertTrue(crawlerRequest.getHeaders().containsKey(headerKey1));
    Assertions.assertEquals(headerValue1, crawlerRequest.getHeaders().get(headerKey1));

    Assertions.assertTrue(crawlerRequest.getHeaders().containsKey(headerKey1));
    Assertions.assertEquals(headerValue1, crawlerRequest.getHeaders().get(headerKey1));

    Assertions.assertEquals(body, crawlerRequest.getBody());
    Assertions.assertEquals(proxy, crawlerRequest.getProxy());
    Assertions.assertEquals(sleepScheduler, crawlerRequest.getSleepScheduler());

    Assertions.assertEquals(vRequest, crawlerRequest.getInner());

    crawlerRequest.removeProxy();
    Assertions.assertNull(crawlerRequest.getProxy());
  }

}
