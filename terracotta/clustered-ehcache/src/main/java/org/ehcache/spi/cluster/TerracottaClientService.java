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
package org.ehcache.spi.cluster;

import org.ehcache.clustered.entity.api.ClusteredCacheManagerEntity;
import org.ehcache.spi.service.Service;

/**
 * @author Abhilash
 *
 */
public interface TerracottaClientService extends Service {

  /**
   * This fetches the CacheManager Entity from and provides a local view of the
   * server side counter part. This Entity at the server is located using the
   * provided Terracotta URI in configuration.
   * 
   * @return
   */
  ClusteredCacheManagerEntity getCacheManagerEntity() ;

}
