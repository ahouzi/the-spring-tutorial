--INSERT INTO customer(id, firstname, lastname, signupdate) values( nextval( 'hibernate_sequence') , '%s', '%s', NOW());
-- INSERT INTO customer(id, firstname, lastname, signupdate) values( nextval( 'hibernate_sequence') , 'Juergen', 'Hoeller', NOW());
-- INSERT INTO customer(id, firstname, lastname, signupdate) values( nextval( 'hibernate_sequence') , 'Mark', 'Fisher', NOW());
-- INSERT INTO customer(id, firstname, lastname, signupdate) values( nextval( 'hibernate_sequence') , 'Rod', 'Johnson', NOW());
-- INSERT INTO customer(id, firstname, lastname, signupdate) values( nextval( 'hibernate_sequence') , 'David', 'Syer', NOW());
-- INSERT INTO customer(id, firstname, lastname, signupdate) values( nextval( 'hibernate_sequence') , 'Gunnar', 'Hillert', NOW());
-- INSERT INTO customer(id, firstname, lastname, signupdate) values( nextval( 'hibernate_sequence') , 'Dave', 'McCrory', NOW());
-- INSERT INTO customer(id, firstname, lastname, signupdate) values( nextval( 'hibernate_sequence') , 'Josh', 'Long', NOW());
-- INSERT INTO customer(id, firstname, lastname, signupdate) values( nextval( 'hibernate_sequence') , 'Patrick', 'Chanezon', NOW());
-- INSERT INTO customer(id, firstname, lastname, signupdate) values( nextval( 'hibernate_sequence') , 'Andy', 'Piper', NOW());
-- INSERT INTO customer(id, firstname, lastname, signupdate) values( nextval( 'hibernate_sequence') , 'Eric', 'Bottard', NOW());
-- INSERT INTO customer(id, firstname, lastname, signupdate) values( nextval( 'hibernate_sequence') , 'Chris', 'Richardson', NOW());
-- INSERT INTO customer(id, firstname, lastname, signupdate) values( nextval( 'hibernate_sequence') , 'Raja', 'Rao', NOW());
-- INSERT INTO customer(id, firstname, lastname, signupdate) values( nextval( 'hibernate_sequence') , 'Rajdeep', 'Dua', NOW());
-- INSERT INTO customer(id, firstname, lastname, signupdate) values( nextval( 'hibernate_sequence') , 'Monica', 'Wilkinson', NOW());
-- INSERT INTO customer(id, firstname, lastname, signupdate) values( nextval( 'hibernate_sequence') , 'Mark', 'Pollack', NOW());


 -- this table is used by Spring Social to store user connection information
 create table UserConnection (userId varchar(255) not null,
	providerId varchar(255) not null,
	providerUserId varchar(255),
	rank int not null,
	displayName varchar(255),
	profileUrl varchar(512),
	imageUrl varchar(512),
	accessToken varchar(255) not null,
	secret varchar(255),
	refreshToken varchar(255),
	expireTime bigint,
	primary key (userId, providerId, providerUserId));
create unique index UserConnectionRank on UserConnection(userId, providerId, rank);