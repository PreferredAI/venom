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

package ai.preferred.venom.utils;

import ai.preferred.venom.request.Request;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * A utility for managing URLs.
 *
 * @author Ween Jiann Lee
 */
public final class UrlUtil {

  /**
   * Prevent construction of UrlUtil.
   */
  private UrlUtil() {

  }

  /**
   * Get base url from a request.
   *
   * @param request an instance of request
   * @return base URL string
   * @throws URISyntaxException if not a proper URL
   */
  public static String getBaseUrl(final Request request) throws URISyntaxException {
    final URI uri = new URI(request.getUrl());
    final URI baseUri = new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), uri.getPath(), null, null);
    return baseUri.toString();
  }

}
