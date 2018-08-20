set echo on
drop directory ut_process_log;
drop role dblogging;
--
create role dblogging;
create directory ut_process_log_dir as  '/scratch/dblogging';
grant read, write on directory ut_process_log_dir to dblogging;
grant read, write on directory ut_process_log_dir to sa;
create public synonym ut_process_log_dir for ut_process_log_dir;
/* sa should be declare */
grant  dblogging to sa; 
exit
