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

package ai.preferred.venom.storage;

import ai.preferred.venom.fetcher.Callback;
import ai.preferred.venom.request.Request;
import ai.preferred.venom.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class FakeFileManager implements FileManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileManager.class);

  private final Callback callback;

  private final Map<Request, Record> requestRecordMap;

  private final AtomicBoolean closed = new AtomicBoolean(false);

  public FakeFileManager() {
    this(new HashMap<>());
  }

  public FakeFileManager(final Map<Request, Record> requestRecordMap) {
    this.callback = new FileManagerCallback(this);
    this.requestRecordMap = requestRecordMap;
  }

  @Override
  public @NotNull Callback getCallback() {
    return callback;
  }

  @Override
  public @NotNull String put(@NotNull Request request, @NotNull Response response) {
    requestRecordMap.put(request, StorageRecord.builder().build());
    LOGGER.info("Put called for request: {}", request.getUrl());
    return "true";
  }

  @Override
  public @NotNull Record get(Object id) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public @NotNull Record get(@NotNull Request request) throws StorageException {
    for (Map.Entry<Request, Record> requestRecordEntry : requestRecordMap.entrySet()) {
      final Request trueRequest = requestRecordEntry.getKey();

      if (trueRequest == null) {
        throw new StorageException("Throw code captured.");
      }

      if (trueRequest.getUrl().equals(request.getUrl())
          && trueRequest.getMethod() == trueRequest.getMethod()
          && trueRequest.getHeaders().equals(request.getHeaders())) {
        if ((trueRequest.getBody() != null && request.getBody() != null
            && trueRequest.getBody().equals(request.getBody()))
            || (trueRequest.getBody() == null && request.getBody() == null)) {
          return requestRecordEntry.getValue();
        }
      }
    }
    LOGGER.info("Get return none for request: {}", request.getUrl());
    return null;
  }

  @Override
  public void close() {
    closed.set(true);
  }

  public boolean getClosed() {
    return closed.get();
  }
}
