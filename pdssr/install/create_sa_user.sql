set define on
set echo on
spool create_sa_user
alter session set container = sales_reporting_pdb;
drop user sa cascade;
grant connect to  SA identified  by tutorial container=local;
grant connect to  SA identified  by tutorial;

grant create session to SA;
grant create sequence to SA;
grant create table to SA;
grant create procedure to SA;
grant create type to SA;
grant create view to SA;
-- grant create directory to SA;
alter user SA default tablespace sales_reporting;
alter user SA quota unlimited on sales_reporting;


--
create role dblogging;
grant execute on sys.dbms_monitor to dblogging;
create directory ut_process_log_dir as  '/scratch/dblogging';
grant select on sys.v_$mystat to dblogging;
grant read, write on directory ut_process_log_dir to dblogging;
grant read, write on directory ut_process_log_dir to SA;
--create public synonym ut_process_log_dir for ut_process_log_dir;
/* SA should be declare */
grant  dblogging to SA; 
grant select on sys.v_$mystat to SA with grant option;
exit
-- 
--grant execute on sys.utl_file to SA with grant option;
--grant execute on sys.dbms_pipe to SA with grant option;
--grant select on sys.v_$session to SA with grant option;
--grant select on sys.v_$sesstat  to SA with grant option;
--grant execute on sys.dbms_lock to SA with grant option;
--
--grant execute on sys.utl_http to SA with grant option;
--
