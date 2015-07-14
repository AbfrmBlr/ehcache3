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

import org.ehcache.clustered.entity.api.ClientSideCacheManagerEntity;
import org.terracotta.entity.EntityClientEndpoint;

/**
 * 
 * @author Abhilash
 *
 */

public class CacheManagerEntity implements ClientSideCacheManagerEntity {

  private final EntityClientEndpoint endpoint;

  public CacheManagerEntity(EntityClientEndpoint endpoint) {
    this.endpoint = endpoint;
  }

  @Override
  public void close() {
    endpoint.close();
  }

  @Override
  public void handleMessage(byte[] payload) {
    // TODO Based on the higher 32 bits find cache

  }
  
}
