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

import org.ehcache.CacheManager;
import org.ehcache.CacheManagerBuilder;
import org.ehcache.ClusteredCacheManager;
import org.ehcache.spi.cluster.TerracottaClientService;
import org.ehcache.spi.service.ServiceCreationConfiguration;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Alex Snaps
 */
public class TerracottaConfiguration implements ServiceCreationConfiguration<TerracottaClientService>, CacheManagerConfiguration<ClusteredCacheManager> {

  private final List<URI> stripes = new ArrayList<URI>();
  private final TerracottaEntityLifeCycleMode lifecycleMode;

  public TerracottaConfiguration(String serverPoolId, URI uri, URI... uris) {
    this.lifecycleMode = TerracottaEntityLifeCycleMode.GET;
    this.stripes.add(uri);
    if (uris != null && uris.length != 0) {
      this.stripes.addAll(Arrays.asList(uris));
    }
  }

  public TerracottaConfiguration(TerracottaEntityLifeCycleMode lifecycleMode, URI uri, URI... uris) {
    this.lifecycleMode = lifecycleMode;
    this.stripes.add(uri);
    if (uris != null && uris.length != 0) {
      this.stripes.addAll(Arrays.asList(uris));
    }
  }

  @Override
  public CacheManagerBuilder<ClusteredCacheManager> builder(final CacheManagerBuilder<? extends CacheManager> other) {
    return (CacheManagerBuilder<ClusteredCacheManager>) other.using(this);
  }

  public List<URI> getTerracottaURIs() {
    return this.stripes;
  }

  public TerracottaEntityLifeCycleMode getLifeCycleMode() {
    return this.lifecycleMode;
  }

  @Override
  public Class<TerracottaClientService> getServiceType() {
    return TerracottaClientService.class;
  }

}
