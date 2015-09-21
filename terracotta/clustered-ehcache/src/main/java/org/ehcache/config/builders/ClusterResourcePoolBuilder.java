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
package org.ehcache.config.builders;

import java.util.HashMap;
import java.util.Map;

import org.ehcache.cluster.resources.Cluster;
import org.ehcache.config.ResourcePool;
import org.ehcache.config.ResourcePoolImpl;
import org.ehcache.config.ResourceUnit;
import org.ehcache.config.units.MemoryUnit;

/**
 * @author Abhilash
 *
 */
public class ClusterResourcePoolBuilder {
  
  private final Map<String, ResourcePool> resourcePools = new HashMap<String, ResourcePool>();

  private ClusterResourcePoolBuilder() {
    
  }
  
  public static ClusterResourcePoolBuilder newClusterResourcePoolsBuilder() {
    return new ClusterResourcePoolBuilder();
  }
  
  public ClusterResourcePoolBuilder with(String poolAlias, Cluster type, long size, ResourceUnit unit, boolean persistent) {
    this.resourcePools.put(poolAlias, new ResourcePoolImpl(type, size, unit, persistent));
    return this;
  }
  
  public ClusterResourcePoolBuilder offheap(String poolAlias, long size, MemoryUnit unit, boolean persistent) {
    this.resourcePools.put(poolAlias, new ResourcePoolImpl(Cluster.OFFHEAP, size, unit, false));
    return this;
  }
  
  public ClusterResourcePoolBuilder hybrid(String poolAlias, long size, MemoryUnit unit, boolean persistent) {
    this.resourcePools.put(poolAlias, new ResourcePoolImpl(Cluster.HYBRID, size, unit, persistent));
    return this;
  }

  public Map<String, ResourcePool> build() {
    return resourcePools;
  }
  
  
}
