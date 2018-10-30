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

import javax.validation.constraints.NotNull;

/**
 * @author Maksim Tkachenko
 * @author Truong Quoc Tuan
 */
public interface FileManager extends AutoCloseable {

  /**
   * Get callback upon completion of request.
   * <p>
   * Please note that blocking callbacks will significantly reduce the rate
   * at which request are processed. Please implement your own executors on
   * I/O blocking callbacks.
   * </p>
   *
   * @return Callback for FileManager
   */
  @NotNull
  Callback getCallback();

  /**
   * Puts record into database.
   *
   * @param request  request
   * @param response Response
   * @return id of record
   * @throws StorageException throws StorageException
   */
  @NotNull
  String put(@NotNull Request request, @NotNull Response response) throws StorageException;

  /**
   * Returns record by the internal record id.
   *
   * @param id record id
   * @return stored record
   * @throws StorageException throws StorageException
   */
  @NotNull
  Record get(int id) throws StorageException;

  /**
   * Returns latest record matching request.
   *
   * @param request request
   * @return stored record
   * @throws StorageException throws StorageException
   */
  @NotNull
  Record get(@NotNull Request request) throws StorageException;

}
