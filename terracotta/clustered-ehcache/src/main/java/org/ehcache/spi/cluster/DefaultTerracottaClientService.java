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
package org.ehcache.spi.cluster;

import java.net.URI;

import org.ehcache.clustered.config.CacheManagerEntityConfiguration;
import org.ehcache.clustered.entity.api.ClientSideCacheManagerEntityProvider;
import org.ehcache.clustered.entity.api.ClusteredCacheManagerEntity;
import org.ehcache.clustered.entity.exceptions.EntityAccessException;
import org.ehcache.config.TerracottaConfiguration;
import org.ehcache.config.TerracottaEntityLifeCycleMode;
import org.ehcache.connection.EntityConnectionFactory;
import org.ehcache.spi.ServiceProvider;
import org.ehcache.spi.service.ServiceConfiguration;
import org.ehcache.spi.service.ServiceCreationConfiguration;
import org.terracotta.connection.Connection;
import org.terracotta.connection.ConnectionException;

/**
 * @author Abhilash
 *
 */
public class DefaultTerracottaClientService implements TerracottaClientService {

  private Connection connection = null;
  private TerracottaConfiguration configuration = null;
  private TerracottaEntityLifeCycleMode lifeCycleMode = TerracottaEntityLifeCycleMode.GET;
  private volatile ClusteredCacheManagerEntity cacheManagerEntity = null;
  private ClientSideCacheManagerEntityProvider cacheManagerEntityProvider;
  private final ServiceCreationConfiguration<TerracottaClientService> config ;
  
  public DefaultTerracottaClientService(ServiceCreationConfiguration<TerracottaClientService> serviceConfiguration) {
    this.config = serviceConfiguration ;
  }

  @Override
  public void start(ServiceProvider serviceProvider) {

    if (config == null) {
      throw new IllegalArgumentException("CacheManagerEntityProvider Service failed to start. Terracotta Configuration cannot be null");
    }

    if (config.getClass().isAssignableFrom(TerracottaConfiguration.class)) {
      configuration = TerracottaConfiguration.class.cast(config);
      if (configuration.getTerracottaURIs() == null || configuration.getTerracottaURIs().size() == 0) {
        throw new IllegalArgumentException("CacheManagerEntityProvider Service failed to start. Terracotta URIs must be provided");
      }

      if (configuration.getLifeCycleMode() != null) {
        lifeCycleMode = configuration.getLifeCycleMode();
      }

      // For now we need to just handle one URI
      // Going Forward it needs to handle multiple URIs
      try {
        connection = EntityConnectionFactory.connect(configuration.getTerracottaURIs().get(0));
      } catch (ConnectionException e) {
        throw new IllegalStateException("CacheManagerEntityProvider Service failed to start.", e);
      }
      if (connection != null) {
        cacheManagerEntityProvider = new ClientSideCacheManagerEntityProvider(new CacheManagerEntityConfiguration() {

          @Override
          public String getEntityID() {
            return getEntityId(configuration.getTerracottaURIs().get(0));
          }
        }, connection);

        switch (lifeCycleMode) {
        case GET:
          try {
            cacheManagerEntity = cacheManagerEntityProvider.getClientSideCacheManagerEntity();
          } catch (EntityAccessException e) {
            throw new IllegalStateException("CacheManagerEntityProvider Service failed to start.", e);
          }
          break;

        case GET_IF_CONFIG_SAME:
          // Need to implement a life cycle operation
          // Ideally voltron should provide an api to fetch
          // the entity config based on entityID

          break;

        case CREATE:
        case CREATE_DESTROY:
          try {
            cacheManagerEntityProvider.createClientSideCacheManagerEntity();
            cacheManagerEntity = cacheManagerEntityProvider.getClientSideCacheManagerEntity();
          } catch (EntityAccessException e) {
            throw new IllegalStateException("CacheManagerEntityProvider Service failed to start.", e);
          }

          break;

        default:
          throw new IllegalStateException("CacheManagerEntityProvider Service failed to start.");
        }
      }
    }

  }

  @Override
  public void stop() {
    switch (lifeCycleMode) {
    case CREATE_DESTROY:
      try {
        cacheManagerEntityProvider.destroyClientSideCacheManagerEntity();
      } catch (EntityAccessException e) {
        throw new IllegalStateException("CacheManagerEntityProvider Service failed to stop.");
      }
      break;

    case GET:
    case GET_IF_CONFIG_SAME:
    case CREATE:
      break;

    default:
      throw new IllegalStateException("CacheManagerEntityProvider Service failed to stop.");
    }
  }

  private static String getEntityId(URI tcUri) {
    return tcUri.getPath();
  }

  @Override
  public ClusteredCacheManagerEntity getCacheManagerEntity() {
    if (cacheManagerEntity == null) {
      throw new IllegalStateException("CacheManagerEntityProvider Service failed to start.");
    }
    return this.cacheManagerEntity;
  }

}
