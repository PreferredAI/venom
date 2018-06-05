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

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.DecompressingEntity;
import org.apache.http.client.entity.DeflateInputStream;
import org.apache.http.client.entity.InputStreamFactory;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;

import java.util.Locale;
import java.util.zip.GZIPInputStream;

/**
 * Modified from: org.apache.http.client.protocol.ResponseContentEncoding
 *
 * @author Maksim Tkachenko
 */
public class ResponseDecompressor {

  private final static InputStreamFactory DEFLATE = DeflateInputStream::new;
  private final static InputStreamFactory GZIP = GZIPInputStream::new;

  private final Lookup<InputStreamFactory> decoderRegistry;

  public ResponseDecompressor() {
    this.decoderRegistry = RegistryBuilder.<InputStreamFactory>create()
        .register("gzip", GZIP)
        .register("x-gzip", GZIP)
        .register("deflate", DEFLATE)
        .build();
  }

  public void decompress(final HttpResponse response) {
    final HttpEntity entity = response.getEntity();
    if (entity != null && entity.getContentLength() != 0) {
      final Header ceheader = entity.getContentEncoding();
      if (ceheader != null) {
        final HeaderElement[] codecs = ceheader.getElements();
        for (final HeaderElement codec : codecs) {
          final String codecName = codec.getName().toLowerCase(Locale.ROOT);
          final InputStreamFactory decoderFactory = decoderRegistry.lookup(codecName);
          if (decoderFactory != null) {
            response.setEntity(new DecompressingEntity(response.getEntity(), decoderFactory));
            response.removeHeaders("Content-Length");
            response.removeHeaders("Content-Encoding");
            response.removeHeaders("Content-MD5");
          }
        }
      }
    }
  }

}
