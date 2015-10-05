package org.ehcache.clustered;

import org.ehcache.clustered.config.EntityVersion;
import org.ehcache.clustered.config.ServerCacheManagerConfiguration;
import org.ehcache.clustered.entity.api.ClusteredCacheManagerEntity;
import org.terracotta.connection.Connection;
import org.terracotta.connection.entity.EntityMaintenanceRef;
import org.terracotta.leader.CoordinationService;

import java.util.concurrent.Callable;

/**
 * @author Alex Snaps
 */
public class ClusteredCacheManagerEntityManager {

  private final Connection connection;
  private final CoordinationService coordinationService;

  /**
   * Still misses the actual implementation of this CoordinationService!
   * @param connection
   */
  public ClusteredCacheManagerEntityManager(final Connection connection) {
    this(connection, null); // Create a new CoordinationService instead here!
  }

  /**
   * For testing purposes only!
   * @param connection
   * @param coordinationService
   */
  ClusteredCacheManagerEntityManager(final Connection connection, final CoordinationService coordinationService) {
    if(connection == null || coordinationService == null) {
      throw new NullPointerException("Null arg!");
    }

    this.connection = connection;
    this.coordinationService = coordinationService;
  }

  /**
   * Document this
   *
   * @param name the uniquely named CacheManager to create
   * @param config the config to use
   * @param accessMode how to deal with this
   * @return
   */
  public ClusteredCacheManagerEntity getCacheManager(final String name, final ServerCacheManagerConfiguration config,
                                                     final AccessMode accessMode) {

    if (config == null) {
      throw new NullPointerException("ServerCacheManagerConfiguration can't be null");
    }

    ClusteredCacheManagerEntity clusteredCacheManagerEntity = coordinationService.executeIfLeader(ClusteredCacheManagerEntity.class, name, new Callable<ClusteredCacheManagerEntity>() {
      @Override
      public ClusteredCacheManagerEntity call() throws Exception {
        EntityMaintenanceRef<ClusteredCacheManagerEntity, ServerCacheManagerConfiguration> mmodeRef =
            connection.acquireMaintenanceModeRef(ClusteredCacheManagerEntity.class, EntityVersion.getVersion(), name);
        ClusteredCacheManagerEntity clusteredCacheManagerEntity = null;
        if (accessMode.compareTo(AccessMode.VALIDATE) < 0) {
          try {
            switch (accessMode) {
              case DESTROY_CREATE:
                try {
                  mmodeRef.destroy();
                } catch (AssertionError e) {
                  if (!"Unexpected exception".equals(e.getMessage())) {
                    throw e;
                  }
                }
              case CREATE:
                mmodeRef.create(null);
                break;
              default:
                throw new IllegalArgumentException();
            }
          } finally {
            mmodeRef.close();
          }

          clusteredCacheManagerEntity = connection
              .getEntityRef(ClusteredCacheManagerEntity.class, EntityVersion.getVersion(), name).fetchEntity();
          clusteredCacheManagerEntity.init(config);
        }
        return clusteredCacheManagerEntity;
      }
    });

    if (clusteredCacheManagerEntity == null) {
      clusteredCacheManagerEntity = connection
          .getEntityRef(ClusteredCacheManagerEntity.class, EntityVersion.getVersion(), name).fetchEntity();
    }

    final ServerCacheManagerConfiguration configuration =clusteredCacheManagerEntity.getConfiguration();

    if (!configuration.appliesTo(config)) {
      coordinationService.delist(ClusteredCacheManagerEntity.class, name);
      throw new IllegalArgumentException("Config doesn't match!");
    }

    return clusteredCacheManagerEntity;
  }

  /**
   * Document this
   *
   * @param name the CacheManager to destroy
   */
  public void destroyCacheManager(final String name) {
    try {
      if(coordinationService.executeIfLeader(ClusteredCacheManagerEntity.class, name, new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
          EntityMaintenanceRef<ClusteredCacheManagerEntity, ServerCacheManagerConfiguration> mmodeRef =
              connection.acquireMaintenanceModeRef(ClusteredCacheManagerEntity.class, EntityVersion.getVersion(), name);
          mmodeRef.destroy();
          return true;
        }
      }) == null) {
        throw new IllegalStateException("Other clients still enlisted!");
      }
    } finally {
      coordinationService.delist(ClusteredCacheManagerEntity.class, name);
    }
  }

  /**
   * Document each instance here
   */
  public enum AccessMode {
    CREATE,
    DESTROY_CREATE,
    VALIDATE,
    ;
  }
}
