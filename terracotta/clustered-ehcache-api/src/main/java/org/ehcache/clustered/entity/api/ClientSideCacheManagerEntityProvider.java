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
package org.ehcache.clustered.entity.api;

import org.ehcache.clustered.config.CacheManagerEntityConfiguration;
import org.ehcache.clustered.entity.exceptions.EntityAccessException;
import org.terracotta.connection.Connection;
import org.terracotta.connection.entity.EntityMaintenanceRef;
import org.terracotta.connection.entity.EntityRef;

/**
 * @author Abhilash
 *
 */
public class ClientSideCacheManagerEntityProvider {

  private volatile ClientSideCacheManagerEntity cacheManagerEntity;
  private final CacheManagerEntityConfiguration cacheManagerConfiguration;
  private final Connection connection;

  public ClientSideCacheManagerEntityProvider(CacheManagerEntityConfiguration cacheManagerConfiguration, Connection connection) {
    this.cacheManagerConfiguration = cacheManagerConfiguration;
    this.connection = connection;
  }

  public void createClientSideCacheManagerEntity() throws EntityAccessException {
    EntityMaintenanceRef<ClientSideCacheManagerEntity, CacheManagerEntityConfiguration> maintenanceModeRef = connection.acquireMaintenanceModeRef(
        ClientSideCacheManagerEntity.class, cacheManagerConfiguration.getEntityID());
    if (maintenanceModeRef.doesExist()) {
      throw new EntityAccessException("Server Side Entity is already exists.");
    }

    maintenanceModeRef.create(cacheManagerConfiguration);
    maintenanceModeRef.close();

  }

  public ClientSideCacheManagerEntity getClientSideCacheManagerEntity() throws EntityAccessException {
    if (cacheManagerEntity != null) {
      EntityRef<ClientSideCacheManagerEntity, CacheManagerEntityConfiguration> entityRef = connection.getEntityRef(ClientSideCacheManagerEntity.class,
          cacheManagerConfiguration.getEntityID());
      cacheManagerEntity = entityRef.fetchEntity();
      if (cacheManagerEntity == null) {
        throw new EntityAccessException("CacheMananger Entity does not exist.");
      }
    }

    return cacheManagerEntity;
  }

  public void destroyClientSideCacheManagerEntity() throws EntityAccessException {
    EntityMaintenanceRef<ClientSideCacheManagerEntity, CacheManagerEntityConfiguration> maintenanceModeRef = connection.acquireMaintenanceModeRef(
        ClientSideCacheManagerEntity.class, cacheManagerConfiguration.getEntityID());

    if (!maintenanceModeRef.doesExist()) {
      throw new EntityAccessException("Server Side Entity does not exist");
    }

    maintenanceModeRef.destroy();
    maintenanceModeRef.close();
  }

}
