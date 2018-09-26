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

package org.ehcache.clustered.loaderWriter;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.clustered.client.config.builders.ClusteredResourcePoolBuilder;
import org.ehcache.clustered.client.config.builders.ClusteringServiceConfigurationBuilder;
import org.ehcache.clustered.client.internal.UnitTestConnectionService;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.spi.loaderwriter.CacheLoaderWriter;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.ehcache.clustered.client.config.builders.ClusteringServiceConfigurationBuilder.cluster;
import static org.ehcache.config.builders.CacheConfigurationBuilder.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class BasicClusteredLoaderWriterTest {

  private static final URI CLUSTER_URI = URI.create("terracotta://example.com:9540/clustered-loader-writer");

  @Before
  public void definePassthroughServer() throws Exception {
    UnitTestConnectionService.add(CLUSTER_URI,
            new UnitTestConnectionService.PassthroughServerBuilder()
                    .resource("primary-server-resource", 4, MemoryUnit.MB)
                    .build());
  }

  @After
  public void removePassthroughServer() throws Exception {
    UnitTestConnectionService.remove(CLUSTER_URI);
  }

  @Test
  public void testBasicClusteredCacheLoaderWriter() {

    TestCacheLoaderWriter loaderWriter = new TestCacheLoaderWriter();
    CacheConfiguration<Long, String>  cacheConfiguration = newCacheConfigurationBuilder(Long.class, String.class,
            ResourcePoolsBuilder
                    .newResourcePoolsBuilder()
                    .with(ClusteredResourcePoolBuilder.clusteredDedicated("primary-server-resource", 2, MemoryUnit.MB)))
            .withLoaderWriter(loaderWriter)
            .build();

    CacheManager cacheManager = CacheManagerBuilder
            .newCacheManagerBuilder()
            .with(cluster(CLUSTER_URI).autoCreate())
            .withCache("cache-1", cacheConfiguration)
            .build(true);

    Cache<Long, String> cache = cacheManager.getCache("cache-1", Long.class, String.class);

    cache.put(1L, "1");

    assertThat(cache.get(1L), is("1"));

    assertThat(loaderWriter.storeMap.get(1L), is("1"));

  }

  @Test
  public void testLoaderWriterMultipleClients() {

    TestCacheLoaderWriter loaderWriter = new TestCacheLoaderWriter();

    CacheConfiguration<Long, String>  cacheConfiguration = newCacheConfigurationBuilder(Long.class, String.class,
            ResourcePoolsBuilder
                    .newResourcePoolsBuilder()
                    .with(ClusteredResourcePoolBuilder.clusteredDedicated("primary-server-resource", 2, MemoryUnit.MB)))
            .withLoaderWriter(loaderWriter)
            .build();

    CacheManager cacheManager1 = CacheManagerBuilder
            .newCacheManagerBuilder()
            .with(cluster(CLUSTER_URI).autoCreate())
            .withCache("cache-1", cacheConfiguration)
            .build(true);

    CacheManager cacheManager2 = CacheManagerBuilder
            .newCacheManagerBuilder()
            .with(cluster(CLUSTER_URI).autoCreate())
            .withCache("cache-1", cacheConfiguration)
            .build(true);

    Cache<Long, String> client1 = cacheManager1.getCache("cache-1", Long.class, String.class);
    Cache<Long, String> client2 = cacheManager2.getCache("cache-1", Long.class, String.class);

    client1.put(1L, "1");
    client2.put(1L, "2");

    assertThat(client1.get(1L), is("2"));
    assertThat(loaderWriter.storeMap.get(1L), is("2"));

    client1.remove(1L);

    assertThat(client1.get(1L), nullValue());
    assertThat(loaderWriter.storeMap.get(1L), nullValue());

  }

  @Test
  public void testCASOpsMultipleClients() {
    TestCacheLoaderWriter loaderWriter = new TestCacheLoaderWriter();

    CacheConfiguration<Long, String>  cacheConfiguration = newCacheConfigurationBuilder(Long.class, String.class,
            ResourcePoolsBuilder
                    .newResourcePoolsBuilder()
                    .with(ClusteredResourcePoolBuilder.clusteredDedicated("primary-server-resource", 2, MemoryUnit.MB)))
            .withLoaderWriter(loaderWriter)
            .build();

    CacheManager cacheManager1 = CacheManagerBuilder
            .newCacheManagerBuilder()
            .with(cluster(CLUSTER_URI).autoCreate())
            .withCache("cache-1", cacheConfiguration)
            .build(true);

    CacheManager cacheManager2 = CacheManagerBuilder
            .newCacheManagerBuilder()
            .with(cluster(CLUSTER_URI).autoCreate())
            .withCache("cache-1", cacheConfiguration)
            .build(true);

    Cache<Long, String> client1 = cacheManager1.getCache("cache-1", Long.class, String.class);
    Cache<Long, String> client2 = cacheManager2.getCache("cache-1", Long.class, String.class);

    assertThat(client1.putIfAbsent(1L, "1"), nullValue());
    assertThat(client2.putIfAbsent(1L, "2"), is("1"));

    assertThat(client1.get(1L), is("1"));
    assertThat(loaderWriter.storeMap.get(1L), is("1"));

    assertThat(client1.replace(1L, "2"), is("1"));
    assertThat(client2.replace(1L, "3"), is("2"));

    assertThat(client1.get(1L), is("3"));
    assertThat(loaderWriter.storeMap.get(1L), is("3"));

    assertThat(client1.replace(1L, "2", "4"), is(false));
    assertThat(client2.replace(1L, "3", "4"), is(true));

    assertThat(client1.get(1L), is("4"));
    assertThat(loaderWriter.storeMap.get(1L), is("4"));

    assertThat(client1.remove(1L, "5"), is(false));
    assertThat(client2.remove(1L, "4"), is(true));

  }

  @Test
  public void testBulkOps() throws Exception {
    TestCacheLoaderWriter loaderWriter = new TestCacheLoaderWriter();
    CacheConfiguration<Long, String>  cacheConfiguration = newCacheConfigurationBuilder(Long.class, String.class,
            ResourcePoolsBuilder
                    .newResourcePoolsBuilder()
                    .with(ClusteredResourcePoolBuilder.clusteredDedicated("primary-server-resource", 2, MemoryUnit.MB)))
            .withLoaderWriter(loaderWriter)
            .build();

    CacheManager cacheManager = CacheManagerBuilder
            .newCacheManagerBuilder()
            .with(cluster(CLUSTER_URI).autoCreate())
            .withCache("cache-1", cacheConfiguration)
            .build(true);

    Cache<Long, String> cache = cacheManager.getCache("cache-1", Long.class, String.class);

    Map<Long, String> mappings = new HashMap<>();

    for (int i = 1; i <= 5; i++) {
      mappings.put((long) i, "" + i);
    }

    cache.putAll(mappings);

    assertThat(loaderWriter.storeMap.keySet(), containsInAnyOrder(mappings.keySet().toArray()));

    cache.clear();

    Map<Long, String> loadedData = cache.getAll(mappings.keySet());

    assertThat(mappings.keySet(), containsInAnyOrder(loadedData.keySet().toArray()));

    cache.removeAll(mappings.keySet());

    assertThat(loaderWriter.storeMap.isEmpty(), is(true));
  }

}
