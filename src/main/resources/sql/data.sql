truncate table video;
insert into video(name, description, username) values ('Learn Spring boot 3', 'Nice framework', 'user');
insert into video(name, description, username) values ('Learn Spring boot data jdbc', 'Nice project', 'user');
insert into video(name, description, username) values ('Learn Spring boot data jpa', 'Useful project', 'user');

truncate table user_account;
insert into user_account(username, password, authorities) values('lambda', 'lambda', '{READER}');