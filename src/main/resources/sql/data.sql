truncate table video;
insert into video(name, description) values ('Learn Spring boot 3', 'Nice framework');
insert into video(name, description) values ('Learn Spring boot data jdbc', 'Nice project');
insert into video(name, description) values ('Learn Spring boot data jpa', 'Useful project');

truncate table user_account;
insert into user_account(username, password, authorities) values('lambda', 'lambda', '{READER}');