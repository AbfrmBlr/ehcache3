/*
 * Copyright Terracotta, Inc.
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
package org.ehcache.clustered.codecs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.ehcache.clustered.config.CacheManagerEntityConfiguration;

/**
 * @author Abhilash
 *
 */
public class ConfigurationCodec {

  public static byte[] encodeCacheManangerConfiguration(CacheManagerEntityConfiguration config) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    DataOutputStream dataOutputStream = new DataOutputStream(out);
    dataOutputStream.writeUTF(config.getEntityID());
    dataOutputStream.close();
    return out.toByteArray();
  }

  public static CacheManagerEntityConfiguration decodeCacheManangerConfiguration(byte[] cacheManagerConfig) throws IOException {
    ByteArrayInputStream in = new ByteArrayInputStream(cacheManagerConfig);
    DataInputStream dataInputStream =  new DataInputStream(in);
    final String entityId = dataInputStream.readUTF();
    return new CacheManagerEntityConfiguration() {

      @Override
      public String getEntityID() {
        return entityId;
      }

    };
  }
  
}
