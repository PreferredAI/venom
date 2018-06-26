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

package ai.preferred.venom.fetcher;

import ai.preferred.venom.request.Request;
import ai.preferred.venom.response.BaseResponse;
import ai.preferred.venom.response.Response;
import ai.preferred.venom.validator.Validator;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;

import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class TestFetcher implements Fetcher {

  public enum Status {
    COMPLETE,
    FAILED
  }

  private final AtomicInteger counter = new AtomicInteger();

  private final LinkedList<Status> statuses;

  public TestFetcher(LinkedList<Status> statuses) {
    this.statuses = statuses;
  }

  public int getCounter() {
    return counter.get();
  }

  private static final FutureCallback<Response> EMPTY_CALLBACK = new FutureCallback<Response>() {
    @Override
    public void completed(Response result) {

    }

    @Override
    public void failed(Exception ex) {

    }

    @Override
    public void cancelled() {

    }
  };

  @Override
  public void start() {

  }

  @Override
  public @NotNull Future<Response> fetch(@NotNull Request request) {
    return fetch(request, EMPTY_CALLBACK);
  }

  @Override
  public @NotNull Future<Response> fetch(@NotNull Request request, @NotNull FutureCallback<Response> callback) {
    final ExecutorService executor = Executors.newSingleThreadExecutor();

    return executor.submit(() -> {
      final int statusCode = 200;
      final String baseUrl = request.getUrl();
      final byte[] content = "IPSUM".getBytes();
      final ContentType contentType = ContentType.create("text/html", StandardCharsets.UTF_8);
      final Header[] headers = {};
      final HttpHost proxy = request.getProxy();

      final Response response = new BaseResponse(statusCode, baseUrl, content, contentType, headers, proxy);

      final Status status = statuses.poll();
      counter.incrementAndGet();
      if (status == Status.COMPLETE) {
        callback.completed(response);
      } else {
        callback.failed(new ValidationException(Validator.Status.INVALID_CONTENT, response, "Call to fail."));
      }

      return response;
    });
  }

  @Override
  public void close() {

  }
}
