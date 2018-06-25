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
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class EmptyContentValidatorTest {

  private final Request request = new VRequest("https://venom.preferred.ai");
  private final int statusCode = 200;
  private final String baseUrl = "https://venom.preferred.ai";
  private final ContentType contentType = ContentType.create("text/html", StandardCharsets.UTF_8);
  private final Header[] headers = {};
  private final HttpHost proxy = null;

  @Test
  public void testEmptyContent() {
    final byte[] content = "".getBytes();
    final Response response = new BaseResponse(statusCode, baseUrl, content, contentType, headers, proxy);
    Assert.assertEquals(Validator.Status.INVALID_CONTENT, EmptyContentValidator.INSTANCE.isValid(request, response));
  }

  @Test
  public void testValidContent() {
    final byte[] content = "IPSUM".getBytes();
    final Response response = new BaseResponse(statusCode, baseUrl, content, contentType, headers, proxy);
    Assert.assertEquals(Validator.Status.VALID, EmptyContentValidator.INSTANCE.isValid(request, response));
  }

}
