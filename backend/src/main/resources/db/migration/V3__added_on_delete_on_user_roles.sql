ALTER TABLE pharmacy.user_roles
DROP CONSTRAINT user_roles_user_id_fkey;

ALTER TABLE pharmacy.user_roles
ADD CONSTRAINT user_roles_user_id_fkey
FOREIGN KEY (user_id) REFERENCES pharmacy.users(id) ON DELETE CASCADE;