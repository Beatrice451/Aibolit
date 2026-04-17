-- ${flyway:timestamp}
INSERT INTO pharmacy.roles (role_name)
VALUES ('USER'),
       ('MANAGER'),
       ('ADMIN'),
       ('PHARMACIST')
ON CONFLICT DO NOTHING;
