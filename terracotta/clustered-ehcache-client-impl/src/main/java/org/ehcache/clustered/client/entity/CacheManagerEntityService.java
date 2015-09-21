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
import org.ehcache.clustered.config.CacheManagerEntityConfiguration;
import org.ehcache.clustered.entity.api.ClientSideCacheManagerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.entity.EntityClientEndpoint;
import org.terracotta.entity.EntityClientService;

/**
 * 
 * @author Abhilash
 *
 */

public class CacheManagerEntityService implements EntityClientService<ClientSideCacheManagerEntity, CacheManagerEntityConfiguration> {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(CacheManagerEntityService.class);

  @Override
  public boolean handlesEntityType(Class<ClientSideCacheManagerEntity> cls) {
    return cls.isAssignableFrom(ClientSideCacheManagerEntity.class); // for now
  }

  @Override
  public ClientSideCacheManagerEntity create(EntityClientEndpoint endpoint) {
    return new CacheManagerEntity(endpoint);
  }

  @Override
  public byte[] serializeConfiguration(CacheManagerEntityConfiguration configuration) {
    byte[] config = null;
    try {
      config = ConfigurationCodec.encodeCacheManangerConfiguration(configuration);
    } catch (IOException e) {
      LOGGER.error("Failed to encode Entity Config", e);
      throw new IllegalStateException("Failed to encode Entity Config", e);
    }
    return config;
  }

  @Override
  public CacheManagerEntityConfiguration deserializeConfiguration(byte[] configuration) {
    CacheManagerEntityConfiguration config = null;
    try {
      config = ConfigurationCodec.decodeCacheManangerConfiguration(configuration);
    } catch (IOException e) {
      LOGGER.error("Failed to decode Entity Config", e);
      throw new IllegalStateException("Failed to decode Entity Config", e);
    }
    return config;
  }

}
