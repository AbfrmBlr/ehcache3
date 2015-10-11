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
package org.ehcache.connection;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.connection.DisconnectHandler;

/**
 * 
 * @author Abhilash
 *
 */

public class EntityDisconnectHandler implements DisconnectHandler {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(EntityDisconnectHandler.class);

  @Override
  public void connectionLost(URI uri) {
    // TODO Auto-generated method stub
    LOGGER.error("Platform detected a disconnect from Server. Do as required");
  }

}
