set define on
set echo on
alter session set container = sales_reporting_pdb;
grant connect to  &username_name identified  by tutorial;
grant create session to &username_name;
grant create sequence to &username_name;
grant create table to &username_name;
grant create procedure to &username_name;
grant create type to &username_name;
grant create view to &username_name;
-- grant create directory to &username_name;
--alter user &username_name default tablespace sales_reporting;
--alter user &username_name quota unlimited on sales_reporting;


--
create role dblogging;
create directory job_msg_dir as  '/scratch/dblogging';
grant select on sys.v_$mystat to dblogging;
grant read, write on directory job_msg_dir to dblogging;
grant read, write on directory job_msg_dir to &username_name;
--create public synonym job_msg_dir for job_msg_dir;
/* &username_name should be declare */
grant  dblogging to &username_name; 
grant select on sys.v_$mystat to &username_name with grant option;
exit
-- 
--grant execute on sys.utl_file to &username_name with grant option;
--grant execute on sys.dbms_pipe to &username_name with grant option;
--grant select on sys.v_$session to &username_name with grant option;
--grant select on sys.v_$sesstat  to &username_name with grant option;
--grant execute on sys.dbms_lock to &username_name with grant option;
--
--grant execute on sys.utl_http to &username_name with grant option;
--
