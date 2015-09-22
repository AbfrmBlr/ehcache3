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


import org.ehcache.cluster.resources.Cluster;
import org.ehcache.cluster.resources.ClusterResourcePool;
import org.ehcache.cluster.resources.ClusterResourcePoolImpl;
import org.ehcache.config.ResourceUnit;

/**
 * @author Abhilash
 *
 */
public class ClusterResourcePoolBuilder {
  
  private ClusterResourcePool resourcePool;

  private ClusterResourcePoolBuilder() {
    
  }
  
  public static ClusterResourcePoolBuilder newClusterResourcePoolsBuilder() {
    return new ClusterResourcePoolBuilder();
  }
  
  public ClusterResourcePoolBuilder with(String poolAlias, Cluster type, long size, ResourceUnit unit, boolean persistent) {
    this.resourcePool = new ClusterResourcePoolImpl(type, size, unit, persistent, poolAlias);
    return this;
  }

  public ClusterResourcePool build() {
    return this.resourcePool;
  }
  
  
}
