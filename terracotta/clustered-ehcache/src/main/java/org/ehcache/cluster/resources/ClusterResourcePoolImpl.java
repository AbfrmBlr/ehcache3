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
package org.ehcache.cluster.resources;

import org.ehcache.config.ResourcePoolImpl;
import org.ehcache.config.ResourceType;
import org.ehcache.config.ResourceUnit;

/**
 * @author Abhilash
 *
 */
public class ClusterResourcePoolImpl extends ResourcePoolImpl implements ClusterResourcePool {
  
  private final String serverPoolId;
  
  public ClusterResourcePoolImpl(ResourceType type, long size, ResourceUnit unit, boolean persistent, String serverPoolId) {
    super(type, size, unit, persistent);
    this.serverPoolId = serverPoolId;
  }

  @Override
  public String getServerPoolId() {
    return this.serverPoolId;
  }

}
