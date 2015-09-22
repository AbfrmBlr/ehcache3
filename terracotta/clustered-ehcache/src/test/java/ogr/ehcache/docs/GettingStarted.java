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
package ogr.ehcache.docs;


import java.net.URI;
import java.net.URISyntaxException;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.CacheManagerBuilder;
import org.ehcache.cluster.resources.Cluster;
import org.ehcache.config.ResourcePoolsBuilder;
import org.ehcache.config.TerracottaConfiguration;
import org.ehcache.config.TerracottaEntityLifeCycleMode;
import org.ehcache.config.builders.ClusterResourcePoolBuilder;
import org.ehcache.config.builders.ClusteredCacheConfigurationBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Abhilash
 *
 */
public class GettingStarted {
  
  @Test
  @Ignore
  public void createClusteredCacheManagerOnly() throws URISyntaxException {
    
    //If the client has rights to create/dstroy. Ideally only some admin clients should be allowed to do it.
    TerracottaConfiguration configuration = new TerracottaConfiguration(TerracottaEntityLifeCycleMode.CREATE_DESTROY, new URI("terracotta://localhost:9510/cachemanager1"));
    
    TerracottaConfiguration getOnlyconfiguration = new TerracottaConfiguration(TerracottaEntityLifeCycleMode.GET, new URI("terracotta://localhost:9510/cachemanager2"));
    
    CacheManager clusteredCacheManager = CacheManagerBuilder.newCacheManagerBuilder().with(configuration).build(true);
  }
  
  @Test
  @Ignore
  public void createClusteredCache() throws URISyntaxException {
    
    TerracottaConfiguration configuration = new TerracottaConfiguration(TerracottaEntityLifeCycleMode.CREATE_DESTROY, new URI("terracotta://localhost:9510/cachemanager1"));
    
    CacheManager clusteredCacheManager = CacheManagerBuilder.newCacheManagerBuilder().with(configuration).build(true);
    
    Cache<String, String> cache = clusteredCacheManager.createCache("simple", ClusteredCacheConfigurationBuilder.newCacheConfigurationBuilder()
                                                                                .withResourcePools(ResourcePoolsBuilder.newResourcePoolsBuilder()
                                                                                    .heap(100, EntryUnit.ENTRIES)
                                                                                    .offheap(5, MemoryUnit.GB))
                                                                                .withClusterResourcePools(ClusterResourcePoolBuilder.newClusterResourcePoolsBuilder()
                                                                                    .with("offHeap", Cluster.OFFHEAP, 10, MemoryUnit.GB, true)) 
                                                                                .buildConfig(String.class, String.class));
    
    
  }
  

  
}
