INSERT INTO anniversaries (description, anniversary_type, anniversary_year) VALUES ('Masami Kurumada''s Passionate Artwork 40th Anniversary', null,40);
INSERT INTO anniversaries (description, anniversary_type, anniversary_year) VALUES ('Jump 50th Anniversary Edition', null,50);
INSERT INTO anniversaries (description, anniversary_type, anniversary_year) VALUES ('Tamashii Nations 10th World Tour', 'TAMASHII_NATIONS_WORLD_TOUR',10);
INSERT INTO anniversaries (description, anniversary_type, anniversary_year) VALUES ('Tamashii Nations 15th World Tour', 'TAMASHII_NATIONS_WORLD_TOUR',15);
INSERT INTO anniversaries (description, anniversary_type, anniversary_year) VALUES ('10th Anniversary', 'SAINT_CLOTH_MYTH',10);
INSERT INTO anniversaries (description, anniversary_type, anniversary_year) VALUES ('15th Anniversary', 'SAINT_CLOTH_MYTH',15);
INSERT INTO anniversaries (description, anniversary_type, anniversary_year) VALUES ('20th Anniversary', 'SAINT_CLOTH_MYTH',20);
INSERT INTO anniversaries (description, anniversary_type, anniversary_year) VALUES ('Saint Seiya 30th Anniversary Theme Exhibition', 'SAINT_SEIYA',30);
INSERT INTO anniversaries (description, anniversary_type, anniversary_year) VALUES ('Saint Seiya 40th anniversary', 'SAINT_SEIYA',40);

INSERT INTO distributions (description) VALUES ('Stores');
INSERT INTO distributions (description) VALUES ('Tamashii Web Shop');
INSERT INTO distributions (description) VALUES ('Tamashii World Tour');
INSERT INTO distributions (description) VALUES ('Tamashii Nations');
INSERT INTO distributions (description) VALUES ('Tamashii Store');
INSERT INTO distributions (description) VALUES ('Other Limited Edition');

INSERT INTO distributors (name, country, website) VALUES ('BANDAI', 'JP', 'https://tamashii.jp/');
INSERT INTO distributors (name, country, website) VALUES ('DAM', 'MX', 'https://animexico-online.com/');
INSERT INTO distributors (name, country, website) VALUES ('DTM', 'MX', null);
INSERT INTO distributors (name, country, website) VALUES ('BANDAI_CHINA', 'CN', null);
INSERT INTO distributors (name, country, website) VALUES ('DS_DISTRIBUTIONS', 'ES', 'https://www.sddistribuciones.com/');
INSERT INTO distributors (name, country, website) VALUES ('BLUE_FIN', 'US', 'https://www.bluefincorp.com');

INSERT INTO groups (description) VALUES ('Accessories');
INSERT INTO groups (description) VALUES ('Bronze Saint V1');
INSERT INTO groups (description) VALUES ('Bronze Saint V2');
INSERT INTO groups (description) VALUES ('Bronze Saint V3');
INSERT INTO groups (description) VALUES ('Bronze Saint V4');
INSERT INTO groups (description) VALUES ('Bronze Saint V5');
INSERT INTO groups (description) VALUES ('Secondary Bronze');
INSERT INTO groups (description) VALUES ('Black Saint');
INSERT INTO groups (description) VALUES ('Steel');
INSERT INTO groups (description) VALUES ('Silver Saint');
INSERT INTO groups (description) VALUES ('Gold Saint');
INSERT INTO groups (description) VALUES ('God Robe');
INSERT INTO groups (description) VALUES ('Poseidon Scale');
INSERT INTO groups (description) VALUES ('Surplice Saint');
INSERT INTO groups (description) VALUES ('Specter');
INSERT INTO groups (description) VALUES ('Judge');
INSERT INTO groups (description) VALUES ('God');
INSERT INTO groups (description) VALUES ('Gold Inheritor');

INSERT INTO lineups (description) VALUES ('Myth Cloth EX');
INSERT INTO lineups (description) VALUES ('Myth Cloth');
INSERT INTO lineups (description) VALUES ('Appendix');
INSERT INTO lineups (description) VALUES ('Saint Cloth Legend');
INSERT INTO lineups (description) VALUES ('Figuarts');
INSERT INTO lineups (description) VALUES ('Saint Cloth Crown');
INSERT INTO lineups (description) VALUES ('DD Panoramation');
INSERT INTO lineups (description) VALUES ('Figuarts Zero Metallic Touch');
INSERT INTO lineups (description) VALUES ('Saint Cloth Action');
INSERT INTO lineups (description) VALUES ('Saint Cloth Rebirth');
INSERT INTO lineups (description) VALUES ('EX project Metalbuild');

INSERT INTO series (description) VALUES ('Saint Seiya');
INSERT INTO series (description) VALUES ('Saintia Sho');
INSERT INTO series (description) VALUES ('Soul of Gold');
INSERT INTO series (description) VALUES ('Saint Seiya Legend Of Sanctuary');
INSERT INTO series (description) VALUES ('Saint Seiya Omega');
INSERT INTO series (description) VALUES ('The Lost Canvas');
INSERT INTO series (description) VALUES ('Saint Seiya The Beginning');

-- ========================= INITIALIZING BASIC ADMIN ========================================
INSERT INTO roles (description) VALUES ('Admin');

INSERT INTO permissions (description) VALUES ('permissions:delete');
INSERT INTO permissions (description) VALUES ('permissions:read');
INSERT INTO permissions (description) VALUES ('permissions:update');
INSERT INTO permissions (description) VALUES ('permissions:write');

INSERT INTO permissions (description) VALUES ('roles:permissions:assign');
INSERT INTO permissions (description) VALUES ('roles:permissions:read');
INSERT INTO permissions (description) VALUES ('roles:permissions:sync');
INSERT INTO permissions (description) VALUES ('roles:read');
INSERT INTO permissions (description) VALUES ('roles:update');
INSERT INTO permissions (description) VALUES ('roles:write');

INSERT INTO role_permission (role_id, permission_id) VALUES (1, 1);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 2);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 3);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 4);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 5);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 6);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 7);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 8);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 9);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 10);

-- ========================= ADDED REST OF PERMISSIONS TO ADMIN ========================================
INSERT INTO permissions (description) VALUES ('anniversaries:delete');
INSERT INTO permissions (description) VALUES ('anniversaries:read');
INSERT INTO permissions (description) VALUES ('anniversaries:update');
INSERT INTO permissions (description) VALUES ('anniversaries:write');

INSERT INTO permissions (description) VALUES ('catalogs:delete');
INSERT INTO permissions (description) VALUES ('catalogs:read');
INSERT INTO permissions (description) VALUES ('catalogs:update');
INSERT INTO permissions (description) VALUES ('catalogs:write');

INSERT INTO permissions (description) VALUES ('collections:figurines:add');
INSERT INTO permissions (description) VALUES ('collections:figurines:read');
INSERT INTO permissions (description) VALUES ('collections:read');
INSERT INTO permissions (description) VALUES ('collections:delete');
INSERT INTO permissions (description) VALUES ('collections:update');

INSERT INTO permissions (description) VALUES ('distributors:delete');
INSERT INTO permissions (description) VALUES ('distributors:read');
INSERT INTO permissions (description) VALUES ('distributors:update');
INSERT INTO permissions (description) VALUES ('distributors:write');

INSERT INTO permissions (description) VALUES ('figurines:delete');
INSERT INTO permissions (description) VALUES ('figurines:images:add');
INSERT INTO permissions (description) VALUES ('figurines:images:delete');
INSERT INTO permissions (description) VALUES ('figurines:images:read');
INSERT INTO permissions (description) VALUES ('figurines:events:add');
INSERT INTO permissions (description) VALUES ('figurines:events:delete');
INSERT INTO permissions (description) VALUES ('figurines:events:read');
INSERT INTO permissions (description) VALUES ('figurines:events:update');
INSERT INTO permissions (description) VALUES ('figurines:update');
INSERT INTO permissions (description) VALUES ('figurines:write');

INSERT INTO permissions (description) VALUES ('purchases:add');
INSERT INTO permissions (description) VALUES ('purchases:read');
INSERT INTO permissions (description) VALUES ('purchases:update');
INSERT INTO permissions (description) VALUES ('purchases:delete');

INSERT INTO permissions (description) VALUES ('stats:read');

INSERT INTO role_permission (role_id, permission_id) VALUES (1, 11);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 12);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 13);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 14);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 15);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 16);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 17);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 18);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 19);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 20);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 21);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 22);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 23);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 24);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 25);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 26);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 27);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 28);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 29);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 30);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 31);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 32);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 33);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 34);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 35);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 36);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 37);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 38);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 39);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 40);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 41);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 42);
