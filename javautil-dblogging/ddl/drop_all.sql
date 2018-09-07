set echo on 
drop sequence job_log_id_seq;
drop table job_log cascade constraints;
drop table job_msg cascade constraints;
drop table ut_process_stat cascade constraints;
exit;
