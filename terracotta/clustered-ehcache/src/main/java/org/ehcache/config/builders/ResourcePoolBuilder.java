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

import org.ehcache.config.Builder;
import org.ehcache.config.ResourcePool;
import org.ehcache.config.ResourcePoolImpl;
import org.ehcache.config.ResourceType;
import org.ehcache.config.ResourceUnit;

/**
 * This should go in core, if it has to exist
 * This can have other utillity methods as well
 * for different type of resources.
 * Once we have ARC in place, it would be required to allow users to deal 
 * with individual ResourcePool instead of an hierarchy of them
 * @author Abhilash
 *
 */
public class ResourcePoolBuilder implements Builder<ResourcePool> {

  private ResourceType type;
  private long size;
  private ResourceUnit unit;
  private boolean persistent;
  
  private ResourcePoolBuilder() {
  }
  
  public static ResourcePoolBuilder newResourcePoolBuilder() {
    return new ResourcePoolBuilder();
  }
  
  public ResourcePoolBuilder with(ResourceType type, long size, ResourceUnit unit, boolean persistent) {
    this.type = type;
    this.size = size;
    this.unit = unit;
    this.persistent = persistent;
    
    return this;
  }
  
  @Override
  public ResourcePool build() {
    return new ResourcePoolImpl(type, size, unit, persistent);
  }

}
