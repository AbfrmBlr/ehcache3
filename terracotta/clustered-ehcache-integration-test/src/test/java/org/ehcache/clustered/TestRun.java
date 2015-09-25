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
package org.ehcache.clustered;

import org.ehcache.clustered.client.entity.ClientSideCacheManagerEntityService;
import org.ehcache.clustered.config.ServerCacheManagerConfiguration;
import org.ehcache.clustered.config.EntityVersion;
import org.ehcache.clustered.entity.api.ClusteredCacheManagerEntity;
import org.ehcache.clustered.server.entity.ServerSideCacheManagerEntityService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.terracotta.connection.Connection;
import org.terracotta.connection.entity.EntityMaintenanceRef;
import org.terracotta.connection.entity.EntityRef;
import org.terracotta.corestorage.StorageManager;
import org.terracotta.entity.EntityClientService;
import org.terracotta.entity.ServerEntityService;
import org.terracotta.entity.Service;
import org.terracotta.entity.ServiceConfiguration;
import org.terracotta.entity.ServiceProvider;
import org.terracotta.entity.ServiceProviderConfiguration;
import org.terracotta.passthrough.PassthroughServer;

import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.fail;

public class TestRun {
  public static final String ENTITY_NAME = "foo";

  private PassthroughServer activeServer;
  private Connection primaryConnection;
  private Connection secondaryConnection;

  @Before
  public void setUp() {
    this.activeServer = new PassthroughServer(true);
    ServerEntityService<?, ?> serverEntityService = new ServerSideCacheManagerEntityService();
    EntityClientService<?, ?> clientEntityService = new ClientSideCacheManagerEntityService();
    this.activeServer.registerServerEntityService(serverEntityService);
    this.activeServer.registerClientEntityService(clientEntityService);
    this.activeServer.registerServiceProviderForType(StorageManager.class, new TestServiceProvider());
    this.activeServer.start();
    this.primaryConnection = this.activeServer.connectNewClient();
    this.secondaryConnection = this.activeServer.connectNewClient();
  }

  @After
  public void tearDown() {
    try {
      this.primaryConnection.close();
      this.secondaryConnection.close();
    } catch (Exception e) {
      fail();
    }
    this.activeServer.stop();
  }

  @Test
  public void singleClientSingleServer() throws Throwable {
    // Create an instance which we expect to succeed.
    long implementationVersion = EntityVersion.getVersion();
    EntityMaintenanceRef<ClusteredCacheManagerEntity, ServerCacheManagerConfiguration> mmodeRef = this.primaryConnection
        .acquireMaintenanceModeRef(ClusteredCacheManagerEntity.class, implementationVersion, ENTITY_NAME);
    try {
      mmodeRef.create(new TestConfig());
    } finally {
      mmodeRef.close();
    }

    // Open the connection to the ref on the primary.
    EntityRef<ClusteredCacheManagerEntity> primaryRef = this.primaryConnection.getEntityRef(ClusteredCacheManagerEntity.class, implementationVersion, ENTITY_NAME);
    ClusteredCacheManagerEntity primaryEntity = primaryRef.fetchEntity();
    Assert.assertTrue(null != primaryEntity);
    primaryEntity.close();

    // Make sure we can also open the connection on the secondary.
    EntityRef<ClusteredCacheManagerEntity> secondaryRef = this.secondaryConnection.getEntityRef(ClusteredCacheManagerEntity.class, implementationVersion, ENTITY_NAME);
    ClusteredCacheManagerEntity secondaryEntity = secondaryRef.fetchEntity();
    Assert.assertTrue(null != secondaryEntity);
    secondaryEntity.close();

    // We should now be able to get the maintenance mode ref since the monitor should be released.
    mmodeRef = this.primaryConnection
        .acquireMaintenanceModeRef(ClusteredCacheManagerEntity.class, implementationVersion, ENTITY_NAME);
    try {
      mmodeRef.destroy();
    } finally {
      mmodeRef.close();
    }
  }


  private static class TestConfig implements ServerCacheManagerConfiguration {
  }

  private static class TestService implements Service<StorageManager> {
    @Override
    public void initialize(ServiceConfiguration<? extends StorageManager> configuration) {
      // TODO Auto-generated method stub

    }

    @Override
    public StorageManager get() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void destroy() {
      // TODO Auto-generated method stub
    }
  }

  private static class TestServiceProvider implements ServiceProvider {
    @Override
    public void close() throws IOException {
      // TODO Auto-generated method stub
    }

    @Override
    public boolean initialize(ServiceProviderConfiguration configuration) {
      // TODO Auto-generated method stub
      return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Service<T> getService(long consumerID, ServiceConfiguration<T> configuration) {
      return (Service<T>)new TestService();
    }

    @Override
    public Collection<Class<?>> getProvidedServiceTypes() {
      // TODO Auto-generated method stub
      return null;
    }
  }
}