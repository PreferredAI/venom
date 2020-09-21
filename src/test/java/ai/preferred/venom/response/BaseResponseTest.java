package ai.preferred.venom.response;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BaseResponseTest {

  @Test
  public void testBaseResponse() {
    final String path = "/test-response";
    final String headerKey = "Cookie";
    final String headerValue = "text=json;";
    final String url = "http://127.0.0.1" + path;

    final int statusCode = 200;
    final String contentStr = "This is a test";
    final byte[] content = contentStr.getBytes();
    final ContentType contentType = ContentType.TEXT_PLAIN;
    final Header[] headers = {new BasicHeader(headerKey, headerValue)};
    final HttpHost proxy = new HttpHost("127.0.0.1", 80);

    final BaseResponse baseResponse = new BaseResponse(statusCode, url, content, contentType, headers, proxy);

    Assertions.assertEquals(statusCode, baseResponse.getStatusCode());
    Assertions.assertEquals(content, baseResponse.getContent());
    Assertions.assertEquals(contentType, baseResponse.getContentType());
    Assertions.assertEquals(headers, baseResponse.getHeaders());
    Assertions.assertEquals(url, baseResponse.getUrl());
    Assertions.assertEquals(proxy, baseResponse.getProxy());

  }

}
