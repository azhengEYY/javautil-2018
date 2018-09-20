set echo on 


  create sequence job_log_id_seq cache 1000;

  CREATE TABLE job_log
   (    job_log_ID NUMBER(9,0),
        SCHEMA_NAME          VARCHAR2(30),
        PROCESS_NAME         VARCHAR2(128),
        THREAD_NAME          VARCHAR2(128),
        PROCESS_RUN_NBR      NUMBER(9,0),
        STATUS_MSG           VARCHAR2(256),
        STATUS_TS 	     TIMESTAMP (6),
        TOTAL_ELAPSED        INTERVAL DAY (2) TO SECOND (6),
        SID                  NUMBER,
        SERIAL#              NUMBER,
        IGNORE_FLG           VARCHAR2(1) DEFAULT 'N',
        MODULE_NAME          VARCHAR2(64),
        CLASS_NAME           VARCHAR2(255),
        trace_file_name      varchar2(255),
         CHECK ( IGNORE_FLG IN ('Y', 'N')) ENABLE,
         CONSTRAINT job_log_PK PRIMARY KEY (job_log_ID)
   );


  CREATE TABLE job_msg
   (    job_msg_ID 	NUMBER(9,0),
        job_log_ID 	NUMBER(9,0),
        LOG_MSG_ID 		VARCHAR2(8),
        LOG_MSG 		VARCHAR2(256),
        LOG_MSG_CLOB 		CLOB,
        LOG_MSG_TS 		TIMESTAMP (6),
        ELAPSED_TIME 		INTERVAL DAY (2) TO SECOND (6),
        LOG_SEQ_NBR 		NUMBER(18,0) NOT NULL ENABLE,
        CALLER_NAME 		VARCHAR2(100),
        LINE_NBR 		NUMBER(5,0),
        CALL_STACK 		CLOB,
        LOG_LEVEL 		NUMBER(2,0),
         CONSTRAINT job_msg_PK PRIMARY KEY (job_log_ID, LOG_SEQ_NBR)
  );

alter table job_msg 
add constraint upl_ups_fk 
foreign key (job_log_id) 
references job_log(job_log_id);
  CREATE TABLE job_stat 
  (    
	job_log_ID 	NUMBER(9,0) NOT NULL ENABLE,
        LOG_SEQ_NBR 		NUMBER(9,0) NOT NULL ENABLE,
        SID 			NUMBER,
        STATISTIC# 		NUMBER,
        VALUE 			NUMBER,
         CONSTRAINT job_stat_PK PRIMARY KEY (job_log_ID, LOG_SEQ_NBR, STATISTIC#)
   ) organization index;

alter table job_stat
add constraint up_process_stat_fk 
              foreign key(job_log_id, log_seq_nbr)
references job_msg(job_log_id, log_seq_nbr);
exit;
