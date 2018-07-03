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

package ai.preferred.venom;

import ai.preferred.venom.request.VRequest;
import ai.preferred.venom.validator.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

public class UrlRouterTest {

  @Test
  public void testUrlRouterNoDefault() {
    final Handler handler = (request, response, scheduler, session, worker) -> {

    };
    final Validator validator = (request, response) -> null;

    final UrlRouter urlRouter = new UrlRouter();
    urlRouter.register(Pattern.compile("pass"), handler);
    urlRouter.register(Pattern.compile("pass"), validator);

    Assertions.assertEquals(validator, urlRouter.getValidator(new VRequest("pass")));
    Assertions.assertEquals(handler, urlRouter.getHandler(new VRequest("pass")));

    Assertions.assertEquals(Validator.ALWAYS_VALID, urlRouter.getValidator(new VRequest("fail")));
    Assertions.assertThrows(RuntimeException.class, () -> urlRouter.getHandler(new VRequest("fail")));

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

    Assertions.assertEquals(validator, urlRouter.getValidator(new VRequest("pass")));
    Assertions.assertEquals(handler, urlRouter.getHandler(new VRequest("pass")));

    Assertions.assertEquals(defaultHandler, urlRouter.getHandler(new VRequest("fail")));
    Assertions.assertEquals(Validator.ALWAYS_VALID, urlRouter.getValidator(new VRequest("fail")));
  }

}
