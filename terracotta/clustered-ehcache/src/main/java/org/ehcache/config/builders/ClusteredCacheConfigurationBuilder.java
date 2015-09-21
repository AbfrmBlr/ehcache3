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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.ehcache.config.EvictionPrioritizer;
import org.ehcache.config.EvictionVeto;
import org.ehcache.config.ResourcePool;
import org.ehcache.config.ResourcePools;
import org.ehcache.config.ResourcePoolsBuilder;
import org.ehcache.config.TerracottaBaseCacheConfiguration;
import org.ehcache.config.TerracottaCacheConfiguration;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.expiry.Expiry;
import org.ehcache.spi.service.ServiceConfiguration;


/**
 * A lot of things done here are already done in CacheConfiguration Builder
 * but things are different in clustered world, so a different class with 
 * some code duplication. May be some design hint required.
 * 
 * NOTE :- Most probably UDT's for Expiry and Evictions are not a possiblity
 * But would it be sane idea to allow user to still define UDT for tiers other than locally
 * and to choose a Terracotta provided one for clustered tier. 
 * 
 * @author Abhilash
 *
 */
public class ClusteredCacheConfigurationBuilder<K, V> {
  
  private final Collection<ServiceConfiguration<?>> serviceConfigurations = new HashSet<ServiceConfiguration<?>>();
  
  private Expiry<? super K, ? super V> expiry;
  private EvictionPrioritizer<? super K, ? super V> evictionPrioritizer;
  private EvictionVeto<? super K, ? super V> evictionVeto;
  
  private Map<String, ResourcePool> clusterResourcePools = new HashMap<String, ResourcePool>();
  private ResourcePools resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder().heap(Long.MAX_VALUE, EntryUnit.ENTRIES).build();
  
  private ClusteredCacheConfigurationBuilder() {
    
  }
  
  private ClusteredCacheConfigurationBuilder(ClusteredCacheConfigurationBuilder<? super K, ? super V> other) {
    this.resourcePools = other.resourcePools;
  }

  public static <K, V> ClusteredCacheConfigurationBuilder<K, V> newCacheConfigurationBuilder() {
    return new ClusteredCacheConfigurationBuilder<K, V>();
  }
  
  public <CK extends K, CV extends V> TerracottaCacheConfiguration<CK, CV> buildConfig(Class<CK> keyType, Class<CV> valueType) {
    return new TerracottaBaseCacheConfiguration<CK, CV>(keyType, valueType, null, null);
  }
  
  public ClusteredCacheConfigurationBuilder<K, V> withResourcePools(ResourcePools resourcePools) {
    if (resourcePools == null) {
      throw new NullPointerException("Null resource pools");
    }
    ClusteredCacheConfigurationBuilder<K, V> otherBuilder = new ClusteredCacheConfigurationBuilder<K, V>(this);
    otherBuilder.resourcePools = resourcePools;
    return otherBuilder;
  }
  
  public ClusteredCacheConfigurationBuilder<K, V> withResourcePools(ResourcePoolsBuilder resourcePoolsBuilder) {
    if (resourcePoolsBuilder == null) {
      throw new NullPointerException("Null resource pools builder");
    }
    return withResourcePools(resourcePoolsBuilder.build());
  }
  
  public ClusteredCacheConfigurationBuilder<K, V> withResourcePools(ClusterResourcePoolBuilder clusterResourcePoolsBuilder) {
    if (clusterResourcePoolsBuilder == null) {
      throw new NullPointerException("Null resource pools builder");
    }
    ClusteredCacheConfigurationBuilder<K, V> otherBuilder = new ClusteredCacheConfigurationBuilder<K, V>(this);
    otherBuilder.clusterResourcePools.putAll(clusterResourcePoolsBuilder.build());
    return otherBuilder;
  }
  
  public <NK extends K, NV extends V> ClusteredCacheConfigurationBuilder<NK, NV> usingEvictionPrioritizer(final EvictionPrioritizer<? super NK, ? super NV> evictionPrioritizer) {
    ClusteredCacheConfigurationBuilder<NK, NV> otherBuilder = new ClusteredCacheConfigurationBuilder<NK, NV>(this);
    otherBuilder.evictionPrioritizer = evictionPrioritizer;
    return otherBuilder;
  }
  
  public <NK extends K, NV extends V> ClusteredCacheConfigurationBuilder<NK, NV> evictionVeto(final EvictionVeto<? super NK, ? super NV> veto) {
    ClusteredCacheConfigurationBuilder<NK, NV> otherBuilder = new ClusteredCacheConfigurationBuilder<NK, NV>(this);
    otherBuilder.evictionVeto = veto;
    return otherBuilder;
  }
  
  public <NK extends K, NV extends V> ClusteredCacheConfigurationBuilder<NK, NV> withExpiry(Expiry<? super NK, ? super NV> expiry) {
    if (expiry == null) {
      throw new NullPointerException("Null expiry");
    }
    ClusteredCacheConfigurationBuilder<NK, NV> otherBuilder = new ClusteredCacheConfigurationBuilder<NK, NV>(this);
    otherBuilder.expiry = expiry;
    return otherBuilder;
  }

  public boolean hasDefaultExpiry() {
    return expiry == null;
  }
}
