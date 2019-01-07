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
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class FakeFetcher implements Fetcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(FakeFetcher.class);

  private static final Callback EMPTY_CALLBACK = new Callback() {
    @Override
    public void completed(@NotNull Request request, @NotNull Response response) {

    }

    @Override
    public void failed(@NotNull Request request, @NotNull Exception ex) {

    }

    @Override
    public void cancelled(@NotNull Request request) {

    }
  };

  private final AtomicInteger counter = new AtomicInteger();

  private final Deque<Status> statuses;

  public FakeFetcher(Deque<Status> statuses) {
    this.statuses = statuses;
  }

  public int getCounter() {
    return counter.get();
  }

  @Override
  public void start() {

  }

  @Override
  public @NotNull Future<Response> fetch(@NotNull Request request) {
    return fetch(request, EMPTY_CALLBACK);
  }

  @Override
  public @NotNull Future<Response> fetch(@NotNull Request request, @NotNull Callback callback) {
    final int statusCode = 200;
    final String baseUrl = request.getUrl();
    final byte[] content = "IPSUM".getBytes();
    final ContentType contentType = ContentType.create("text/html", StandardCharsets.UTF_8);
    final Header[] headers = {};
    final HttpHost proxy = request.getProxy();

    final Response response = new BaseResponse(statusCode, baseUrl, content, contentType, headers, proxy);

    final Status status = statuses.poll();
    counter.incrementAndGet();

    LOGGER.debug("Fetching URL: {}", request.getUrl());
    if (status == Status.COMPLETE) {
      LOGGER.debug("Executing completion callback on {}.", request.getUrl());
      callback.completed(request, response);
    } else {
      final Exception ex = new ValidationException(Validator.Status.INVALID_CONTENT, response,
          "Failure expected... Proceeding with tests.");
      LOGGER.debug("Executing failed callback on {}.", request.getUrl(), ex);
      callback.failed(request, ex);
    }

    return new Future<Response>() {
      @Override
      public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
      }

      @Override
      public boolean isCancelled() {
        return false;
      }

      @Override
      public boolean isDone() {
        return true;
      }

      @Override
      public Response get() {
        return response;
      }

      @Override
      public Response get(long timeout, @Nonnull TimeUnit unit) {
        return response;
      }
    };
  }

  @Override
  public void close() {

  }

  public enum Status {
    COMPLETE,
    FAILED
  }
}
