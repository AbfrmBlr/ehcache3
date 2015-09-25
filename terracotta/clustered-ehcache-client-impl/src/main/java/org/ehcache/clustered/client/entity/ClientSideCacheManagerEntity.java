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

import org.ehcache.clustered.entity.api.ClusteredCacheManagerEntity;
import org.terracotta.entity.EndpointDelegate;
import org.terracotta.entity.EntityClientEndpoint;

/**
 * 
 * @author Abhilash
 *
 */

public class ClientSideCacheManagerEntity implements ClusteredCacheManagerEntity {

  private final EntityClientEndpoint endpoint;

  public ClientSideCacheManagerEntity(EntityClientEndpoint endpoint) {
    this.endpoint = endpoint;
    endpoint.setDelegate(new ClientEndpointDelegate());
  }

  @Override
  public void close() {
    endpoint.close();
  }

  private static class ClientEndpointDelegate implements EndpointDelegate {
    @Override
    public void handleMessage(final byte[] payload) {
      // TODO Based on the higher 32 bits find cache

    }

    @Override
    public byte[] createExtendedReconnectData() {
      throw new UnsupportedOperationException("Implement me!");
    }

    @Override
    public void didDisconnectUnexpectedly() {
      throw new UnsupportedOperationException("Implement me!");
    }
  }
}
