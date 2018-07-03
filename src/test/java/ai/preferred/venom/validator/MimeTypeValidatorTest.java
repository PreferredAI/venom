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

package ai.preferred.venom.validator;

import ai.preferred.venom.request.Request;
import ai.preferred.venom.request.VRequest;
import ai.preferred.venom.response.BaseResponse;
import ai.preferred.venom.response.Response;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class MimeTypeValidatorTest {

  private final Request request = new VRequest("https://venom.preferred.ai");
  private final int statusCode = 200;
  private final String baseUrl = "https://venom.preferred.ai";
  private final byte[] content = "IPSUM".getBytes();
  private final Header[] headers = {};
  private final HttpHost proxy = null;

  @Test
  public void testValidMimeTypePattern() {
    final ContentType contentType = ContentType.create("text/html", StandardCharsets.UTF_8);
    final Response response = new BaseResponse(statusCode, baseUrl, content, contentType, headers, proxy);
    Assertions.assertEquals(Validator.Status.VALID,
        new MimeTypeValidator(Pattern.compile("^text.*")).isValid(request, response));
  }

  @Test
  public void testInvalidMimeTypePattern() {
    final ContentType contentType = ContentType.create("text/json", StandardCharsets.UTF_8);
    final Response response = new BaseResponse(statusCode, baseUrl, content, contentType, headers, proxy);
    Assertions.assertEquals(Validator.Status.INVALID_CONTENT,
        new MimeTypeValidator(Pattern.compile("^image.*")).isValid(request, response));
  }

  @Test
  public void testValidMimeTypeString() {
    final ContentType contentType = ContentType.create("text/html", StandardCharsets.UTF_8);
    final Response response = new BaseResponse(statusCode, baseUrl, content, contentType, headers, proxy);
    Assertions.assertEquals(Validator.Status.VALID,
        new MimeTypeValidator("^text.*").isValid(request, response));
  }

  @Test
  public void testInvalidMimeTypeString() {
    final ContentType contentType = ContentType.create("text/json", StandardCharsets.UTF_8);
    final Response response = new BaseResponse(statusCode, baseUrl, content, contentType, headers, proxy);
    Assertions.assertEquals(Validator.Status.INVALID_CONTENT,
        new MimeTypeValidator("^image.*").isValid(request, response));
  }

}
