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
package org.ehcache.clustered.server.entity;

import java.io.IOException;

import org.ehcache.clustered.codecs.ConfigurationCodec;
import org.ehcache.clustered.config.ServerCacheManagerConfiguration;
import org.ehcache.clustered.config.EntityVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.corestorage.StorageManager;
import org.terracotta.entity.BasicServiceConfiguration;
import org.terracotta.entity.ClientCommunicator;
import org.terracotta.entity.PassiveServerEntity;
import org.terracotta.entity.ServerEntityService;
import org.terracotta.entity.Service;
import org.terracotta.entity.ServiceRegistry;

/**
 * The concrete implementation of {@ServerEntityService}
 * which platform will use to instantiate the ServerSideCacheManagerEntity and
 * even the Passive counter part of it
 * 
 * @author Abhilash
 *
 */

public class ServerSideCacheManagerEntityService implements ServerEntityService<ServerSideCacheManagerEntity, PassiveServerEntity> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerSideCacheManagerEntityService.class);

  @Override
  public boolean handlesEntityType(String typeName) {
    return typeName.equals("org.ehcache.clustered.entity.api.ClusteredCacheManagerEntity");
  }

  @Override
  public ServerSideCacheManagerEntity createActiveEntity(ServiceRegistry registry, byte[] configuration) {
    
    ServerCacheManagerConfiguration config = null;

    try {
      config = ConfigurationCodec.decodeCacheManangerConfiguration(configuration);
    } catch (IOException e) {
      LOGGER.error("Failed to decode Entity Config", e);
    }

    if (config == null) {
      throw new IllegalArgumentException("Entity Config cannot be null");
    }

    Service<StorageManager> storageService = registry.getService(new BasicServiceConfiguration<StorageManager>(StorageManager.class));
    Service<ClientCommunicator> communicatorService = registry.getService(new BasicServiceConfiguration<ClientCommunicator>(ClientCommunicator.class));

    if (storageService == null || communicatorService == null) {
      throw new IllegalArgumentException("Storage Service is not configured.");
    }
    return new ServerSideCacheManagerEntity(config, storageService.get(), communicatorService.get());
  }

  @Override
  public PassiveServerEntity createPassiveEntity(ServiceRegistry registry, byte[] configuration) {
    throw new UnsupportedOperationException("Implement Me !");
  }

  @Override
  public long getVersion() {
    return EntityVersion.getVersion();
  }

}
