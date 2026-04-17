-- Столбец добавлен для привязки фармацевтов к определенной аптеке.
-- Смысл показывать фармацевту все аптеки, а не только ту, где он работает?
ALTER TABLE pharmacy.users
    ADD COLUMN pharmacy_id INT REFERENCES pharmacy.pharmacies (id);