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
import org.ehcache.clustered.config.EntityVersion;
import org.ehcache.clustered.config.ServerCacheManagerConfiguration;
import org.ehcache.clustered.entity.api.ClusteredCacheManagerEntity;
import org.ehcache.clustered.server.entity.ServerSideCacheManagerEntityService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.terracotta.connection.Connection;
import org.terracotta.connection.entity.Entity;
import org.terracotta.connection.entity.EntityRef;
import org.terracotta.corestorage.StorageManager;
import org.terracotta.entity.EntityClientService;
import org.terracotta.entity.ServerEntityService;
import org.terracotta.entity.ServiceConfiguration;
import org.terracotta.entity.ServiceProvider;
import org.terracotta.entity.ServiceProviderConfiguration;
import org.terracotta.leader.CoordinationService;
import org.terracotta.passthrough.PassthroughServer;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.concurrent.Callable;

import static org.junit.Assert.fail;

public class TestRun {
  public static final String ENTITY_NAME = "foo";

  private PassthroughServer activeServer;
  private Connection primaryConnection;
  private Connection secondaryConnection;
  private ClusteredCacheManagerEntityManager ccmeManager;

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

    this.ccmeManager = new ClusteredCacheManagerEntityManager(this.primaryConnection, new MyCoordinationService());
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
    final ClusteredCacheManagerEntity cacheManager = ccmeManager.getCacheManager(ENTITY_NAME, new TestConfig(), ClusteredCacheManagerEntityManager.AccessMode.DESTROY_CREATE);
    Assert.assertNotNull(cacheManager);
    cacheManager.close();

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
    ccmeManager.destroyCacheManager(ENTITY_NAME);
  }


  private static class TestConfig implements ServerCacheManagerConfiguration {
    @Override
    public boolean appliesTo(final ServerCacheManagerConfiguration config) {
      return true;
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
    public <T> T getService(long consumerID, ServiceConfiguration<T> configuration) {
      return null;
    }

    @Override
    public Collection<Class<?>> getProvidedServiceTypes() {
      // TODO Auto-generated method stub
      return null;
    }
  }

  private static class MyCoordinationService implements CoordinationService {

    Deque<Thread> clients = new ArrayDeque<Thread>();

    @Override
    public synchronized <T> T executeIfLeader(final Class<? extends Entity> entityType, final String entityName, final Callable<T> callable) {
      final Thread currentThread = Thread.currentThread();
      if (!clients.contains(currentThread)) {
        clients.push(currentThread);
      }
      if (clients.peek() == currentThread) {
        try {
          return callable.call();
        } catch (Exception e) {
          clients.remove(currentThread);
          throw new RuntimeException(e);
        }
      }
      return null;
    }

    @Override
    public synchronized void delist(final Class<ClusteredCacheManagerEntity> clusteredCacheManagerEntityClass, final String name) {
      clients.remove(Thread.currentThread());
    }
  }
}