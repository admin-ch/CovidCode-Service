create table authorization_code
(
	id uuid not null
		constraint authorization_code_pkey
			primary key,
	code varchar(9) not null
		constraint uq_authorization_code_code
			unique,
	creation_date_time timestamp not null,
	expiry_date timestamp not null,
	onset_date date not null,
	call_count integer not null
);
