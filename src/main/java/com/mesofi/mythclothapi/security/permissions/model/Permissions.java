package com.mesofi.mythclothapi.security.permissions.model;

public interface Permissions {

    String PERMISSIONS_DELETE = "permissions:delete";
    String PERMISSIONS_READ = "permissions:read";
    String PERMISSIONS_UPDATE = "permissions:update";
    String PERMISSIONS_WRITE = "permissions:write";

    String ROLES_PERMISSIONS_ASSIGN = "roles:permissions:assign";
    String ROLES_PERMISSIONS_READ = "roles:permissions:read";
    String ROLES_PERMISSIONS_SYNC = "roles:permissions:sync";
    String ROLES_READ = "roles:read";
    String ROLES_UPDATE = "roles:update";
    String ROLES_WRITE = "roles:write";

    String ANNIVERSARIES_DELETE = "anniversaries:delete";
    String ANNIVERSARIES_READ = "anniversaries:read";
    String ANNIVERSARIES_UPDATE = "anniversaries:update";
    String ANNIVERSARIES_WRITE = "anniversaries:write";

    String CATALOGS_DELETE = "catalogs:delete";
    String CATALOGS_READ = "catalogs:read";
    String CATALOGS_UPDATE = "catalogs:update";
    String CATALOGS_WRITE = "catalogs:write";

    String COLLECTIONS_FIGURINES_ADD = "collections:figurines:add";
    String COLLECTIONS_FIGURINES_READ = "collections:figurines:read";
    String COLLECTIONS_FIGURINES_DELETE = "collections:figurines:delete";
    String COLLECTIONS_READ = "collections:read";
    String COLLECTIONS_DELETE = "collections:delete";
    String COLLECTIONS_UPDATE = "collections:update";

    String DISTRIBUTORS_DELETE = "distributors:delete";
    String DISTRIBUTORS_READ = "distributors:read";
    String DISTRIBUTORS_UPDATE = "distributors:update";
    String DISTRIBUTORS_WRITE = "distributors:write";

    String FIGURINES_DELETE = "figurines:delete";
    String FIGURINES_LOAD = "figurines:load";
    String FIGURINES_IMAGES_ADD = "figurines:images:add";
    String FIGURINES_IMAGES_DELETE = "figurines:images:delete";
    String FIGURINES_IMAGES_READ = "figurines:images:read";
    String FIGURINES_EVENTS_ADD = "figurines:events:add";
    String FIGURINES_EVENTS_DELETE = "figurines:events:delete";
    String FIGURINES_EVENTS_READ = "figurines:events:read";
    String FIGURINES_EVENTS_UPDATE = "figurines:events:update";
    String FIGURINES_STORES_READ = "figurines:stores:read";
    String FIGURINES_STORES_ASSIGN = "figurines:stores:assign";
    String FIGURINES_UPDATE = "figurines:update";
    String FIGURINES_WRITE = "figurines:write";

    String PURCHASES_ADD = "purchases:add";
    String PURCHASES_READ = "purchases:read";
    String PURCHASES_UPDATE = "purchases:update";
    String PURCHASES_DELETE = "purchases:delete";

    String STATS_READ = "stats:read";

    String STORES_WRITE = "stores:write";
}
