/*
 * Copyright 2017 Preferred.AI
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

package ai.preferred.venom;

import ai.preferred.venom.request.VRequest;
import ai.preferred.venom.validator.Validator;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.regex.Pattern;

public class UrlRouterTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void testUrlRouterNoDefault() {
    final Handler handler = (request, response, scheduler, session, worker) -> {

    };
    final Validator validator = (request, response) -> null;

    final UrlRouter urlRouter = new UrlRouter();
    urlRouter.register(Pattern.compile("pass"), handler);
    urlRouter.register(Pattern.compile("pass"), validator);

    Assert.assertEquals(validator, urlRouter.getValidator(new VRequest("pass")));
    Assert.assertEquals(handler, urlRouter.getHandler(new VRequest("pass")));

    Assert.assertEquals(Validator.ALWAYS_VALID, urlRouter.getValidator(new VRequest("fail")));
    exception.expect(RuntimeException.class);
    exception.expectMessage("Default handler is not set");
    urlRouter.getHandler(new VRequest("fail"));
  }

  @Test
  public void testUrlRouter() {
    final Handler defaultHandler = (request, response, scheduler, session, worker) -> {
    };

    final Handler handler = (request, response, scheduler, session, worker) -> {
    };
    final Validator validator = (request, response) -> null;

    final UrlRouter urlRouter = new UrlRouter(defaultHandler);
    urlRouter.register(Pattern.compile("pass"), handler);
    urlRouter.register(Pattern.compile("pass"), validator);

    Assert.assertEquals(validator, urlRouter.getValidator(new VRequest("pass")));
    Assert.assertEquals(handler, urlRouter.getHandler(new VRequest("pass")));

    Assert.assertEquals(defaultHandler, urlRouter.getHandler(new VRequest("fail")));
    Assert.assertEquals(Validator.ALWAYS_VALID, urlRouter.getValidator(new VRequest("fail")));
  }

}
