drop sequence job_log_id_seq;
drop sequence job_step_id_seq;
drop TABLE job_log cascade constraints;
drop TABLE job_msg cascade constraints;
drop TABLE job_stat cascade constraints; 
drop package logger;
