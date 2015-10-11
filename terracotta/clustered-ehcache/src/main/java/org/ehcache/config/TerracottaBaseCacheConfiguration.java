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

package org.ehcache.config;

import org.ehcache.spi.service.ServiceConfiguration;

/**
 * @author Alex Snaps
 */
public class TerracottaBaseCacheConfiguration<K, V> extends BaseCacheConfiguration<K, V> implements TerracottaCacheConfiguration<K, V> {

  private ClusteredCacheSharedConfiguration<K, V> cacheSharedConfiguration;

  public TerracottaBaseCacheConfiguration(final ClusteredCacheSharedConfiguration<K, V> cacheSharedConfiguration,
                                          final ClassLoader classLoader,
                                          final ResourcePools resourcePools,
                                          final ServiceConfiguration<?>... serviceConfigurations) {
    super(cacheSharedConfiguration.getKeyType(),
        cacheSharedConfiguration.getValueType(),
        cacheSharedConfiguration.getEvictionVeto(),
        cacheSharedConfiguration.getEvictionPrioritizer(),
        classLoader,
        cacheSharedConfiguration.getExpiry(),
        resourcePools, serviceConfigurations);
    this.cacheSharedConfiguration = cacheSharedConfiguration;
  }

  //The resourcePools has to change to a mapping of poolAlias -> ResourcePool once ARC is in place.
  //This assumption will change as and when ARC evolves
  public TerracottaBaseCacheConfiguration(final Class<K> keyType, final Class<V> valueType, final ClassLoader classLoader, final ResourcePools resourcePools) {
    super(keyType, valueType, null, null, classLoader, null, resourcePools);
  }

  @Override
  public ClusteredCacheSharedConfiguration<K, V> getClusteredCacheConfiguration() {
    return cacheSharedConfiguration;
  }
}