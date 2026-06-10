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

-- =================================================================
INSERT INTO roles (description) VALUES ('Admin');
INSERT INTO roles (description) VALUES ('Basic Collector');

INSERT INTO permissions (description) VALUES ('catalogs:read');
INSERT INTO permissions (description) VALUES ('catalogs:write');
INSERT INTO permissions (description) VALUES ('catalogs:delete');
INSERT INTO permissions (description) VALUES ('catalogs:update');

INSERT INTO role_permission (role_id, permission_id) VALUES (1, 1);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 2);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 3);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 4);
--INSERT INTO role_permissions (role_id, permission_id) VALUES (2, 1);