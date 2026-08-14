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

    /** Allows deletion of permissions. */
    // String PERMISSIONS_DELETE = "permissions:delete";

    /** Allows reading permissions. */
    // String PERMISSIONS_READ = "permissions:read";

    /** Allows updating permissions. */
    // String PERMISSIONS_UPDATE = "permissions:update";

    /** Allows creation of permissions. */
    // String PERMISSIONS_WRITE = "permissions:write";

    /** Allows assigning permissions to roles. */
    // String ROLES_PERMISSIONS_ASSIGN = "roles:permissions:assign";

    /** Allows reading permissions assigned to roles. */
    // String ROLES_PERMISSIONS_READ = "roles:permissions:read";

    /** Allows synchronizing permissions assigned to roles. */
    // String ROLES_PERMISSIONS_SYNC = "roles:permissions:sync";

    /** Allows reading roles. */
    // String ROLES_READ = "roles:read";

    /** Allows updating roles. */
    // String ROLES_UPDATE = "roles:update";

    /** Allows creating roles. */
    // String ROLES_WRITE = "roles:write";

    /** Allows deletion of anniversaries. */
    // String ANNIVERSARIES_DELETE = "anniversaries:delete";

    /** Allows reading anniversaries. */
    // String ANNIVERSARIES_READ = "anniversaries:read";

    /** Allows updating anniversaries. */
    // String ANNIVERSARIES_UPDATE = "anniversaries:update";

    /** Allows creating anniversaries. */
    // String ANNIVERSARIES_WRITE = "anniversaries:write";

    /** Allows deletion of catalogs. */
    // String CATALOGS_DELETE = "catalogs:delete";

    /** Allows reading catalogs. */
    // String CATALOGS_READ = "catalogs:read";

    /** Allows updating catalogs. */
    // String CATALOGS_UPDATE = "catalogs:update";

    /** Allows creating catalogs. */
    // String CATALOGS_WRITE = "catalogs:write";

    /** Allows adding figurines to collections. */
    String COLLECTIONS_FIGURINES_ADD = "collections:figurines:add";

    /** Allows reading figurines in collections. */
    // String COLLECTIONS_FIGURINES_READ = "collections:figurines:read";

    /** Allows removing figurines from collections. */
    // String COLLECTIONS_FIGURINES_DELETE = "collections:figurines:delete";

    /** Allows reading collections. */
    // String COLLECTIONS_READ = "collections:read";

    /** Allows deleting collections. */
    // String COLLECTIONS_DELETE = "collections:delete";

    /** Allows updating collections. */
    // String COLLECTIONS_UPDATE = "collections:update";

    /** Allows deletion of distributors. */
    // String DISTRIBUTORS_DELETE = "distributors:delete";

    /** Allows reading distributors. */
    // String DISTRIBUTORS_READ = "distributors:read";

    /** Allows updating distributors. */
    // String DISTRIBUTORS_UPDATE = "distributors:update";

    /** Allows creating distributors. */
    // String DISTRIBUTORS_WRITE = "distributors:write";

    /** Allows deletion of figurines. */
    // String FIGURINES_DELETE = "figurines:delete";

    /** Allows loading figurine data. */
    // String FIGURINES_LOAD = "figurines:load";

    /** Allows adding images to figurines. */
    // String FIGURINES_IMAGES_ADD = "figurines:images:add";

    /** Allows deleting figurine images. */
    // String FIGURINES_IMAGES_DELETE = "figurines:images:delete";

    /** Allows reading figurine images. */
    // String FIGURINES_IMAGES_READ = "figurines:images:read";

    /** Allows adding events to figurines. */
    // String FIGURINES_EVENTS_ADD = "figurines:events:add";

    /** Allows deleting figurine events. */
    // String FIGURINES_EVENTS_DELETE = "figurines:events:delete";

    /** Allows reading figurine events. */
    // String FIGURINES_EVENTS_READ = "figurines:events:read";

    /** Allows updating figurine events. */
    // String FIGURINES_EVENTS_UPDATE = "figurines:events:update";

    /** Allows reading stores associated with figurines. */
    // String FIGURINES_STORES_READ = "figurines:stores:read";

    /** Allows assigning stores to figurines. */
    // String FIGURINES_STORES_ASSIGN = "figurines:stores:assign";

    /** Allows updating figurines. */
    // String FIGURINES_UPDATE = "figurines:update";

    /** Allows creating figurines. */
    // String FIGURINES_WRITE = "figurines:write";

    /** Allows adding purchases. */
    // String PURCHASES_ADD = "purchases:add";

    /** Allows reading purchases. */
    // String PURCHASES_READ = "purchases:read";

    /** Allows updating purchases. */
    // String PURCHASES_UPDATE = "purchases:update";

    /** Allows deleting purchases. */
    // String PURCHASES_DELETE = "purchases:delete";

    /** Allows reading statistics. */
    // String STATS_READ = "stats:read";

    /** Allows creating or managing stores. */
    // String STORES_WRITE = "stores:write";
}
