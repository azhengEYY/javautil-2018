set define on
define test_dblogger = sr 
set echo on
spool create_user 
alter session set container = sales_reporting_pdb;

grant connect to  &test_dblogger identified  by tutorial container=all;
grant create session to &test_dblogger;
grant create table to &test_dblogger;
grant create procedure to &test_dblogger;
grant create type to &test_dblogger;
grant create view to &test_dblogger;
-- grant create directory to &test_dblogger;
grant create sequence to &test_dblogger;
alter user &test_dblogger default tablespace sales_reporting;
alter user &test_dblogger quota unlimited on sales_reporting;
-- 
#grant execute on sys.utl_file to &test_dblogger with grant option;
#grant execute on sys.dbms_pipe to &test_dblogger with grant option;
#grant select on sys.v_$session to &test_dblogger with grant option;
#grant select on sys.v_$mystat to &test_dblogger with grant option;
#grant select on sys.v_$sesstat  to &test_dblogger with grant option;
#grant execute on sys.dbms_lock to &test_dblogger with grant option;
--
#grant execute on sys.utl_http to &test_dblogger with grant option;
--

create directory ut_process_log_dir as '/common/scratch/ut_process_log_dir';
drop directory udump_dir;
create directory udump_dir as '/common/oracle/diag/rdbms/dev18b/dev18b/trace/';
create public synonym udump_dir for udump_dir;
grant read on directory udump_dir to &test_dblogger;
grant read, write on directory ut_process_log_dir to &test_dblogger;