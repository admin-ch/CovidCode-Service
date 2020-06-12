CREATE DATABASE haauthcodegeneration;
CREATE USER haauthcodegeneration WITH PASSWORD 'secret';
ALTER USER haauthcodegeneration WITH SUPERUSER;
GRANT ALL ON DATABASE haauthcodegeneration TO haauthcodegeneration;

CREATE ROLE haauthcodegeneration_role_full;
GRANT ALL ON DATABASE haauthcodegeneration TO haauthcodegeneration_role_full;
