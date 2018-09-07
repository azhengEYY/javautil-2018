set echo on
drop directory job_msg;
drop role dblogging;
--
create role dblogging;
create directory job_msg_dir as  '/scratch/dblogging';
grant read, write on directory job_msg_dir to dblogging;
grant read, write on directory job_msg_dir to sa;
create public synonym job_msg_dir for job_msg_dir;
/* sa should be declare */
grant  dblogging to sa; 
exit
