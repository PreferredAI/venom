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

import org.apache.commons.lang3.StringUtils;
import org.jsoup.helper.StringUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Borrowed from WebMagic Project: https://github.com/code4craft/webmagic
 *
 * @author Maksim Tkachenko
 */
public class UrlUtils {

  private static String resolveHrefs(String html, String baseUrl, Pattern pattern) {
    final StringBuilder sb = new StringBuilder();
    final Matcher matcher = pattern.matcher(html);
    int lastEnd = 0;
    boolean modified = false;
    while (matcher.find()) {
      modified = true;
      sb.append(StringUtils.substring(html, lastEnd, matcher.start()));
      sb.append(matcher.group(1));
      sb.append('"').append(StringUtil.resolve(baseUrl, matcher.group(2))).append('"');
      lastEnd = matcher.end();
    }
    if (!modified) {
      return html;
    }
    sb.append(StringUtils.substring(html, lastEnd));
    return sb.toString();
  }

  public static String resolveUrls(String html, String baseUrl) {
    html = resolveHrefs(html, baseUrl, Pattern.compile("(<a[^<>]*href=)[\"']([^\"'<>]*)[\"']",
        Pattern.CASE_INSENSITIVE));
    html = resolveHrefs(html, baseUrl, Pattern.compile("(<a[^<>]*href=)([^\"'<>\\s]+)",
        Pattern.CASE_INSENSITIVE));
    return html;
  }

  private UrlUtils() {
    throw new AssertionError();
  }

}
