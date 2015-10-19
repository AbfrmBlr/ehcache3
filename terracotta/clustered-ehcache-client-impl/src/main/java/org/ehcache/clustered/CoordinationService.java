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

package org.ehcache.clustered;

import org.terracotta.connection.entity.Entity;

import java.util.concurrent.Callable;

/**
 * @author Alex Snaps
 */
public interface CoordinationService {

  /*
   *  Constructor creates the entity if it doesn't exist yet. Totally lenient to this being racy.
   *  Entity is initially without any knowledge about anything
   */

  /**
   * If leader, possibly running for election, for this entity type/name pair, execute the {@code callable}
   *
   * @param entityType
   * @param entityName
   * @param callable should be a Function&lt;{@link org.terracotta.connection.Connection}, T&gt;
   * @param <T>
   * @return
   *
   *
   * Something along the line of :
   *   Ticket t;
   *   if(t = entity.runForElection(entityType, entityName) != null) {
   *     try {
   *        return callable.call();
   *       t.accept();
   *     } catch(Throwable t) {
   *       t.cancel();
   *     }
   *   }
   *
   * On the server, entity holds a Queue of all candidates, head is leader.
   * The entity will clear the ticket (and associated locking), should the "in election client" die.
   *
   */
  <T> T executeIfLeader(Class<? extends Entity> entityType, String entityName, Callable<T> callable);

  void delist(Class<? extends Entity> clusteredCacheManagerEntityClass, String name);
}
