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
package org.ehcache.clustered.client.entity;

import java.io.IOException;

import org.ehcache.clustered.codecs.ConfigurationCodec;
import org.ehcache.clustered.config.ServerCacheManagerConfiguration;
import org.ehcache.clustered.entity.api.ClusteredCacheManagerEntity;
import org.terracotta.entity.EntityClientEndpoint;
import org.terracotta.entity.EntityClientService;

/**
 * 
 * @author Abhilash
 *
 */

public class ClientSideCacheManagerEntityService implements EntityClientService<ClusteredCacheManagerEntity, ServerCacheManagerConfiguration> {
  
  @Override
  public boolean handlesEntityType(Class<ClusteredCacheManagerEntity> cls) {
    return cls.isAssignableFrom(ClusteredCacheManagerEntity.class); // for now
  }

  @Override
  public ClusteredCacheManagerEntity create(EntityClientEndpoint endpoint) {
    return new ClientSideCacheManagerEntity(endpoint);
  }

  @Override
  public byte[] serializeConfiguration(ServerCacheManagerConfiguration configuration) {
    byte[] config;
    try {
      config = ConfigurationCodec.encodeCacheManangerConfiguration(configuration);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to encode Entity Config", e);
    }
    return config;
  }

  @Override
  public ServerCacheManagerConfiguration deserializeConfiguration(byte[] configuration) {
    ServerCacheManagerConfiguration config;
    try {
      config = ConfigurationCodec.decodeCacheManangerConfiguration(configuration);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to decode Entity Config", e);
    }
    return config;
  }

}
