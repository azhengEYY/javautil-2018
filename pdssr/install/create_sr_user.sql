set define on
set echo on
spool create_sr_user
alter session set container = sales_reporting_pdb;
drop user sr cascade;
grant connect to  sr identified  by tutorial;

grant create session to sr;
grant create sequence to sr;
grant create table to sr;
grant create procedure to sr;
grant create type to sr;
grant create view to sr;
-- grant create directory to sr;
alter user sr default tablespace sales_reporting;
alter user sr quota unlimited on sales_reporting;


--
create role dblogging;
grant execute on sys.dbms_monitor to dblogging;
create directory ut_process_log_dir as  '/scratch/dblogging';
grant select on sys.v_$mystat to dblogging;
grant read, write on directory ut_process_log_dir to dblogging;
grant read, write on directory ut_process_log_dir to sr;
--create public synonym ut_process_log_dir for ut_process_log_dir;
/* sr should be declare */
grant  dblogging to sr; 
grant select on sys.v_$mystat to sr with grant option;
exit
-- 
--grant execute on sys.utl_file to sr with grant option;
--grant execute on sys.dbms_pipe to sr with grant option;
--grant select on sys.v_$session to sr with grant option;
--grant select on sys.v_$sesstat  to sr with grant option;
--grant execute on sys.dbms_lock to sr with grant option;
--
--grant execute on sys.utl_http to sr with grant option;
--
