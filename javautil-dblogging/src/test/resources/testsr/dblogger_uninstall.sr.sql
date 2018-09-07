drop sequence job_log_id_seq;
drop sequence job_step_id_seq;
drop TABLE job_log cascade constraints;
drop TABLE job_msg cascade constraints;
drop TABLE UT_PROCESS_STAT cascade constraints; 
drop package logger;
