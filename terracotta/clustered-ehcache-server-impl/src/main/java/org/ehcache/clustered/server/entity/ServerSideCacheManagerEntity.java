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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.ehcache.clustered.cache.operations.ClusterOperation;
import org.ehcache.clustered.codecs.ConfigurationCodec;
import org.ehcache.clustered.config.ServerCacheManagerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.corestorage.StorageManager;
import org.terracotta.entity.AbstractDecodingServerEntity;
import org.terracotta.entity.ClientCommunicator;
import org.terracotta.entity.ClientDescriptor;
import org.terracotta.entity.ConcurrencyStrategy;
import org.terracotta.entity.Service;

/**
 * @author Abhilash
 */

public class ServerSideCacheManagerEntity extends AbstractDecodingServerEntity<ClusterOperation, Object> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerSideCacheManagerEntity.class);
  
  private final Service<StorageManager> storageService;
  private final Service<ClientCommunicator> communicatorService;

  private Set<ClientDescriptor> connectedClients = new HashSet<ClientDescriptor>();

  private ReentrantLock clientsStateLock = new ReentrantLock();
  
  private final ConcurrencyStrategy concurrencyStrategy = new EhCacheConcurrencyStrategy();
  
  private final ServerCacheManagerConfiguration configuration;

  public ServerSideCacheManagerEntity(ServerCacheManagerConfiguration config, Service<StorageManager> storageService,  Service<ClientCommunicator> communicatorService) {
    this.storageService = storageService;
    this.communicatorService = communicatorService;
    this.configuration = config;
  }

  @Override
  public byte[] getConfig() {
    byte[] config = null;
    try {
      config = ConfigurationCodec.encodeCacheManangerConfiguration(this.configuration);
    } catch (IOException e) {
      LOGGER.error("Failed to encode Entity Config", e);
    }
    return config;
  }

  @Override
  public void handleReconnect(final ClientDescriptor clientDescriptor, final byte[] extendedReconnectData) {
    throw new UnsupportedOperationException("Implement me!");
  }

  @Override
  public void destroy() {
    storageService.destroy(); // for all services
    communicatorService.destroy();
  }

  @Override
  protected ClusterOperation decodeInput(byte[] bytes) {
    // TODO serialize - serialization would change according to type of operation
    // need to write two kinds of serialization/deserialization strategy
    return null;
  }

  @Override
  protected byte[] encodeOutput(Object o) {
    // TODO Deserialize - need something as OperationResult kind of thing
    return null;
  }

  @Override
  public void connected(ClientDescriptor clientDescriptor) {
    clientsStateLock.lock();
    try {
      connectedClients.add(clientDescriptor);
    } finally {
      clientsStateLock.unlock();
    }
  }

  @Override
  public void disconnected(ClientDescriptor clientDescriptor) {
    clientsStateLock.lock();
    try {
      connectedClients.remove(clientDescriptor);
    } finally {
      clientsStateLock.unlock();
    }
  }

  @Override
  protected Object invoke(ClientDescriptor clientDescriptor, ClusterOperation input) {
    // TODO Auto-generated method stub
    // bring all the operations under one hood
    return null;
  }

  @Override
  public ConcurrencyStrategy getConcurrencyStrategy() {
    return concurrencyStrategy;
  }

  @Override
  public void createNew() {
    
  }

  @Override
  public void loadExisting() {
    // TODO Auto-generated method stub
    // This api is invoked by the platform when the platform restarts or promotes a passive to active
    // This allows entity to recover the state back or to wipe out everything and create a new - totally depends on entity

  }
}
