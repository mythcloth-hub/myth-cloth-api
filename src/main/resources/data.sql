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

INSERT INTO distributors (name, country, website, creation_date, update_date) VALUES ('BANDAI', 'JP', 'https://tamashii.jp/', NOW(), NOW());
INSERT INTO distributors (name, country, website, creation_date, update_date) VALUES ('DAM', 'MX', 'https://animexico-online.com/', NOW(), NOW());
INSERT INTO distributors (name, country, website, creation_date, update_date) VALUES ('DTM', 'MX', null, NOW(), NOW());
INSERT INTO distributors (name, country, website, creation_date, update_date) VALUES ('BANDAI_CHINA', 'CN', null, NOW(), NOW());
INSERT INTO distributors (name, country, website, creation_date, update_date) VALUES ('DS_DISTRIBUTIONS', 'ES', 'https://www.sddistribuciones.com/', NOW(), NOW());
INSERT INTO distributors (name, country, website, creation_date, update_date) VALUES ('BLUE_FIN', 'US', 'https://www.bluefincorp.com', NOW(), NOW());

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
INSERT INTO lineups (description) VALUES ('Saint Cloth Series');
INSERT INTO lineups (description) VALUES ('Tamashii Nations Box');

INSERT INTO series (description) VALUES ('Saint Seiya');
INSERT INTO series (description) VALUES ('Saintia Sho');
INSERT INTO series (description) VALUES ('Soul of Gold');
INSERT INTO series (description) VALUES ('Saint Seiya Legend Of Sanctuary');
INSERT INTO series (description) VALUES ('Saint Seiya Omega');
INSERT INTO series (description) VALUES ('The Lost Canvas');
INSERT INTO series (description) VALUES ('Saint Seiya The Beginning');

INSERT INTO stores (name, code, website, logo_url, currency, country, active, creation_date, update_date) VALUES ('Mandarake', 'MANDARAKE', 'https://order.mandarake.co.jp', 'https://www.mandarake.co.jp/img/global/logo.png', 'JPY', 'JP', true, NOW(), NOW());
INSERT INTO stores (name, code, website, logo_url, currency, country, active, creation_date, update_date) VALUES ('Nin-Nin-Game', 'NIN_NIN_GAME', 'https://www.nin-nin-game.com', 'https://www.nin-nin-game.com/img/logo.png', 'MXN', 'JP', true, NOW(), NOW());
INSERT INTO stores (name, code, website, logo_url, currency, country, active, creation_date, update_date) VALUES ('MyKombini', 'MY_KOMBINI', 'https://mykombini.com', 'https://mykombini-ab5a.kxcdn.com/img/mykombini-logo-1723002204.jpg', 'JPY', 'JP', true, NOW(), NOW());
INSERT INTO stores (name, code, website, logo_url, currency, country, active, creation_date, update_date) VALUES ('Luna Park', 'LUNA_PARK', 'https://www.lunapark.store', 'https://static.wixstatic.com/media/4724a1_42d57453f336409688f3960ca07cc2fa~mv2.png/v1/fill/w_650,h_364,al_c,q_85,usm_0.66_1.00_0.01,enc_avif,quality_auto/LUNA%20PARK%20LOGO_%20REVISE-01.png', 'JPY', 'JP', true, NOW(), NOW());
INSERT INTO stores (name, code, website, logo_url, currency, country, active, creation_date, update_date) VALUES ('Myth Supplies', 'MYTH_SUPPLIES', 'https://mythsupplies.com', 'https://mythsupplies.com/assets/images/web/logo_myth-supplies-header.png', 'MXN', 'MX', true, NOW(), NOW());
INSERT INTO stores (name, code, website, logo_url, currency, country, active, creation_date, update_date) VALUES ('Logan Store', 'LOGAN_STORE', 'https://loganstore.com.mx', 'https://loganstore.com.mx/wp-content/uploads/2021/11/logan-logo_Mesa-de-trabajo-1.svg', 'MXN', 'MX', true, NOW(), NOW());
INSERT INTO stores (name, code, website, logo_url, currency, country, active, creation_date, update_date) VALUES ('Myth Factory', 'MYTH_FACTORY', 'https://www.mythfactoryshop.com', 'https://www.mythfactoryshop.com/cdn-cgi/image/format=webp/img/myth-factory-logo-1742594757.jpg', 'EUR', 'BE', true, NOW(), NOW());

-- ========================= INITIALIZING BASIC ADMIN ========================================
INSERT INTO roles (name, creation_date, update_date) VALUES ('Admin', NOW(), NOW());

INSERT INTO permissions (name, creation_date, update_date) VALUES ('permissions:delete', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('permissions:read', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('permissions:update', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('permissions:write', NOW(), NOW());

INSERT INTO permissions (name, creation_date, update_date) VALUES ('roles:permissions:assign', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('roles:permissions:read', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('roles:permissions:sync', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('roles:read', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('roles:update', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('roles:write', NOW(), NOW());

-- ========================= ADDED REST OF PERMISSIONS TO ADMIN ========================================
INSERT INTO permissions (name, creation_date, update_date) VALUES ('anniversaries:delete', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('anniversaries:read', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('anniversaries:update', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('anniversaries:write', NOW(), NOW());

INSERT INTO permissions (name, creation_date, update_date) VALUES ('catalogs:delete', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('catalogs:read', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('catalogs:update', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('catalogs:write', NOW(), NOW());

INSERT INTO permissions (name, creation_date, update_date) VALUES ('collections:figurines:add', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('collections:figurines:read', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('collections:figurines:delete', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('collections:read', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('collections:delete', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('collections:update', NOW(), NOW());

INSERT INTO permissions (name, creation_date, update_date) VALUES ('distributors:delete', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('distributors:read', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('distributors:update', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('distributors:write', NOW(), NOW());

INSERT INTO permissions (name, creation_date, update_date) VALUES ('figurines:delete', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('figurines:load', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('figurines:images:add', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('figurines:images:delete', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('figurines:images:read', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('figurines:events:add', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('figurines:events:delete', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('figurines:events:read', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('figurines:events:update', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('figurines:stores:read', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('figurines:stores:assign', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('figurines:update', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('figurines:write', NOW(), NOW());

INSERT INTO permissions (name, creation_date, update_date) VALUES ('purchases:add', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('purchases:read', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('purchases:update', NOW(), NOW());
INSERT INTO permissions (name, creation_date, update_date) VALUES ('purchases:delete', NOW(), NOW());

INSERT INTO permissions (name, creation_date, update_date) VALUES ('stats:read', NOW(), NOW());

INSERT INTO permissions (name, creation_date, update_date) VALUES ('stores:write', NOW(), NOW());

INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 1, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 2, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 3, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 4, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 5, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 6, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 7, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 8, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 9, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 10, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 11, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 12, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 13, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 14, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 15, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 16, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 17, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 18, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 19, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 20, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 21, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 22, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 23, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 24, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 25, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 26, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 27, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 28, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 29, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 30, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 31, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 32, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 33, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 34, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 35, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 36, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 37, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 38, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 39, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 40, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 41, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 42, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 43, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 44, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 45, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 46, NOW(), NOW());
INSERT INTO role_permission (role_id, permission_id, creation_date, update_date) VALUES (1, 47, NOW(), NOW());
