truncate table video;
insert into video(name, description, username) values ('Learn Spring boot 3 (test)', 'Nice framework', 'user');
insert into video(name, description, username) values ('Learn Spring boot data jdbc (test)', 'Nice project', 'user');
insert into video(name, description, username) values ('Learn Spring boot data jpa (test)', 'Useful project', 'user');

truncate table user_account;
insert into user_account(username, password, authorities) values('lambda', 'lambda', '{READER}');