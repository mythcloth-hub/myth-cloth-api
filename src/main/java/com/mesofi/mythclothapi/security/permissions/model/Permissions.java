package com.mesofi.mythclothapi.security.permissions.model;

/**
 * Defines the permission authorities used by the application.
 *
 * <p>
 * Permissions follow a resource-based naming convention using the format
 * {@code resource:action}. Some permissions include an additional level when
 * the action applies to a specific sub-resource, such as
 * {@code roles:permissions:assign}.
 * </p>
 *
 * <p>
 * The constants defined by this interface are used by the application's
 * authorization configuration to grant or restrict access to specific
 * operations.
 * </p>
 */
public interface Permissions {

    /** Allows creating anniversaries. */
    String ANNIVERSARIES_CREATE = "anniversaries:create";

    /** Allows deletion of anniversaries. */
    String ANNIVERSARIES_DELETE = "anniversaries:delete";

    /** Allows reading anniversaries. */
    String ANNIVERSARIES_READ = "anniversaries:read";

    /** Allows updating anniversaries. */
    String ANNIVERSARIES_UPDATE = "anniversaries:update";

    /** Allows creating catalogs. */
    String CATALOGS_CREATE = "catalogs:create";

    /** Allows deletion of catalogs. */
    String CATALOGS_DELETE = "catalogs:delete";

    /** Allows reading catalogs. */
    String CATALOGS_READ = "catalogs:read";

    /** Allows updating catalogs. */
    String CATALOGS_UPDATE = "catalogs:update";

    /** Allows deleting collections. */
    String COLLECTIONS_DELETE = "collections:delete";

    /** Allows duplicating collections. */
    String COLLECTIONS_DUPLICATE = "collections:duplicate";

    /** Allows adding figurines to collections. */
    String COLLECTIONS_FIGURINES_ADD = "collections:figurines:add";

    /** Allows deleting figurines from collections. */
    String COLLECTIONS_FIGURINES_DELETE = "collections:figurines:delete";

    /** Allows reading figurines in collections. */
    String COLLECTIONS_FIGURINES_READ = "collections:figurines:read";

    /** Allows reading collections. */
    String COLLECTIONS_READ = "collections:read";

    /** Allows updating collections. */
    String COLLECTIONS_UPDATE = "collections:update";

    /** Allows creating distributors. */
    String DISTRIBUTORS_CREATE = "distributors:create";

    /** Allows deletion of distributors. */
    String DISTRIBUTORS_DELETE = "distributors:delete";

    /** Allows reading distributors. */
    String DISTRIBUTORS_READ = "distributors:read";

    /** Allows updating distributors. */
    String DISTRIBUTORS_UPDATE = "distributors:update";

    /** Allows creating figurines. */
    String FIGURINES_CREATE = "figurines:create";

    /** Allows deleting figurines. */
    String FIGURINES_DELETE = "figurines:delete";

    /** Allows adding events to figurines. */
    String FIGURINES_EVENTS_ADD = "figurines:events:add";

    /** Allows deleting figurine events. */
    String FIGURINES_EVENTS_DELETE = "figurines:events:delete";

    /** Allows reading figurine events. */
    String FIGURINES_EVENTS_READ = "figurines:events:read";

    /** Allows updating figurine events. */
    String FIGURINES_EVENTS_UPDATE = "figurines:events:update";

    /** Allows importing figurine data. */
    String FIGURINES_IMPORT = "figurines:import";

    /** Allows assigning stores to figurines. */
    String FIGURINES_STORES_ASSIGN = "figurines:stores:assign";

    /** Allows ignoring stores associated with figurines. */
    String FIGURINES_STORES_IGNORE = "figurines:stores:ignore";

    /** Allows reading stores associated with figurines. */
    String FIGURINES_STORES_READ = "figurines:stores:read";

    /** Allows reading current prices of figurines from stores. */
    String FIGURINES_STORES_READ_CURRENT_PRICES = "figurines:stores:read-current-prices";

    /** Allows reading historical prices of figurines from stores. */
    String FIGURINES_STORES_READ_HISTORICAL_PRICES = "figurines:stores:read-historical-prices";

    /** Allows updating figurines. */
    String FIGURINES_UPDATE = "figurines:update";

    /** Allows creating permissions. */
    String PERMISSIONS_CREATE = "permissions:create";

    /** Allows deleting permissions. */
    String PERMISSIONS_DELETE = "permissions:delete";

    /** Allows reading permissions. */
    String PERMISSIONS_READ = "permissions:read";

    /** Allows updating permissions. */
    String PERMISSIONS_UPDATE = "permissions:update";

    /** Allows deleting purchases. */
    String PURCHASES_DELETE = "purchases:delete";

    /** Allows creating purchases. */
    String PURCHASES_CREATE = "purchases:create";

    /** Allows reading purchases. */
    String PURCHASES_READ = "purchases:read";

    /** Allows synchronizing purchases. */
    String PURCHASES_SYNC = "purchases:sync";

    /** Allows updating purchases. */
    String PURCHASES_UPDATE = "purchases:update";

    /** Allows creating roles. */
    String ROLES_CREATE = "roles:create";

    /** Allows reading roles' permissions. */
    String ROLES_PERMISSIONS_READ = "roles:permissions:read";

    /** Allows synchronizing roles' permissions. */
    String ROLES_PERMISSIONS_SYNC = "roles:permissions:sync";

    /** Allows reading roles. */
    String ROLES_READ = "roles:read";

    /** Allows updating roles. */
    String ROLES_UPDATE = "roles:update";

    /** Allows reading statistics. */
    String STATS_READ = "stats:read";

    /** Allows creating stores. */
    String STORES_CREATE = "stores:create";

    /** Allows deleting stores. */
    String STORES_DELETE = "stores:delete";

    /** Allows reading stores. */
    String STORES_READ = "stores:read";

    /** Allows updating stores. */
    String STORES_UPDATE = "stores:update";
}
