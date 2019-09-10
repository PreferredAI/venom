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
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class StatusOkValidatorTest {

  private final Request request = new VRequest("https://venom.preferred.ai");
  private final byte[] content = "".getBytes();
  private final String baseUrl = "https://venom.preferred.ai";
  private final ContentType contentType = ContentType.create("text/html", StandardCharsets.UTF_8);
  private final Header[] headers = {};

  private void assertInvalid(int statusCode) {
    final Response response = new BaseResponse(statusCode, baseUrl, content, contentType, headers, null);
    Assertions.assertEquals(Validator.Status.INVALID_STATUS_CODE, StatusOkValidator.INSTANCE.isValid(request, response));
  }

  @Test
  public void testInvalidStatusCode() {
    assertInvalid(400);
    assertInvalid(500);
  }

  @Test
  public void testValidStatusCode() {
    final int statusCode = 200;
    final Response response = new BaseResponse(statusCode, baseUrl, content, contentType, headers, null);
    Assertions.assertEquals(Validator.Status.VALID, StatusOkValidator.INSTANCE.isValid(request, response));
  }

}
