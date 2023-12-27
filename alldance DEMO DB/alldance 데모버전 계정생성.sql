-- 공간(schema)
create database if not exists alldance;
-- 사용자 id/password
create user if not exists 'devdance'@'%'
identified by '12341234';
-- 사용자에게 공간 할당 및 모든 권한 부여
grant all privileges on alldance.*to 'devdance'@'%';
-- 사용자 인증 처리용 비밀번호 지정
alter user 'devdance'@'%'
identified with mysql_native_password by '12341234';
-- 공간 처리 작업 완료
flush privileges;