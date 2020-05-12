ALTER TABLE authorization_code RENAME COLUMN onset_date TO original_onset_date;
ALTER TABLE authorization_code ADD COLUMN onset_date date;
UPDATE authorization_code SET onset_date = (original_onset_date - INTERVAL '3' DAY);
ALTER TABLE authorization_code ALTER COLUMN onset_date SET not null;
