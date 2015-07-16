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
import java.util.Optional;

import org.ehcache.clustered.codecs.ConfigurationCodec;
import org.ehcache.clustered.config.CacheManagerEntityConfiguration;
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

import com.tc.object.EntityID;

/**
 * The concrete implementation of {@ServerEntityService}
 * which platform will use to instantiate the ServerSideCacheManagerEntity and
 * even the Passive counter part of it
 * 
 * @author Abhilash
 *
 */

public class ServerSideCacheManagerEntityService implements ServerEntityService<EntityID, ServerSideCacheManagerEntity, PassiveServerEntity> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerSideCacheManagerEntityService.class);

  @Override
  public boolean handlesEntityType(String typeName) {
    return typeName.equals("org.ehcache.clustered.entity.api.ClientSideCacheManagerEntity");
  }

  @Override
  public ServerSideCacheManagerEntity createActiveEntity(EntityID id, ServiceRegistry registry, byte[] configuration) {
    // Some Comments - Actually voltorn is doing nothing from config. It just
    // provides that to the server side entity
    // which in turn is responsible to understand the config, lookup for
    // required services it needs and then
    // instantiate its server side entity

    CacheManagerEntityConfiguration config = null;

    try {
      config = ConfigurationCodec.decodeCacheManangerConfiguration(configuration);
    } catch (IOException e) {
      LOGGER.error("Failed to decode Entity Config", e);
    }

    if (config == null) {
      throw new IllegalArgumentException("Entity Config cannot be null");
    }

    // The ClusteredCacheManagerConfiguration has the server side pools
    // Once the voltron storage is done the server pool info in config will be
    // used to fetch required
    // storage service abstraction

    // Optional has to go - need to tell this to voltron guys
    // The storage is cooked up for now. Once the Voltron Storage API is nailed,
    // this will change

    Optional<Service<StorageManager>> storageService = registry.getService(new BasicServiceConfiguration<StorageManager>(StorageManager.class));
    Optional<Service<ClientCommunicator>> communicatorService = registry
        .getService(new BasicServiceConfiguration<ClientCommunicator>(ClientCommunicator.class));

    if (!storageService.isPresent() || !communicatorService.isPresent()) {
      // there should be a ConfigurationMismatch or Illegal config exception
      // since entity expected a service to be there but it was absent
      // like createActiveEntity() throws ConfigMisMatchException
      throw new IllegalArgumentException("Storage Service is not configured.");
    }
    return new ServerSideCacheManagerEntity(config, storageService.get(), communicatorService.get());
  }

  @Override
  public PassiveServerEntity createPassiveEntity(EntityID id, ServiceRegistry registry, byte[] configuration) {
    throw new UnsupportedOperationException("Implement Me !");
  }

  @Override
  public long getVersion() {
    return EntityVersion.getVersion();
  }

}
