--#<
-- notes
-- --# ignored line by sqlrunner
-- --< begin extract for markdown (javadoc type)
-- --> end  extract for markdown
-- TODO document specification and write plsql to extract and format
--#<  begin ignore for sqlrunner
-- todo uglify to lower must look for things in quotes
set echo on --#
--#>
  create sequence ut_process_status_id_seq cache 1000;

  CREATE TABLE UT_PROCESS_STATUS
   (    UT_PROCESS_STATUS_ID NUMBER(9,0),
        SCHEMA_NAME          VARCHAR2(30),
        PROCESS_NAME         VARCHAR2(128),
        THREAD_NAME          VARCHAR2(128),
        PROCESS_RUN_NBR      NUMBER(9,0),
        STATUS_MSG           VARCHAR2(256),
        STATUS_ID 	     VARCHAR2(1),
        STATUS_TS 	     TIMESTAMP (6),
        TOTAL_ELAPSED        INTERVAL DAY (2) TO SECOND (6),
        SID                  NUMBER,
        SERIAL#              NUMBER,
        IGNORE_FLG           VARCHAR2(1) DEFAULT 'N',
        MODULE_NAME          VARCHAR2(64),
        CLASS_NAME           VARCHAR2(255),
        trace_file_name      varchar2(255),
         CHECK ( IGNORE_FLG IN ('Y', 'N')) ENABLE,
         CONSTRAINT UT_PROCESS_STATUS_PK PRIMARY KEY (UT_PROCESS_STATUS_ID)
   ); 


  CREATE TABLE UT_PROCESS_LOG
   (    UT_PROCESS_LOG_ID 	NUMBER(9,0),
        UT_PROCESS_STATUS_ID 	NUMBER(9,0),
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
         CONSTRAINT UT_PROCESS_LOG_PK PRIMARY KEY (UT_PROCESS_STATUS_ID, LOG_SEQ_NBR)
  ); 

alter table ut_process_log 
add constraint upl_ups_fk 
foreign key (ut_process_status_id) 
references ut_process_status(ut_process_status_id);

  CREATE TABLE UT_PROCESS_STAT 
  (    
	UT_PROCESS_STATUS_ID 	NUMBER(9,0) NOT NULL ENABLE,
        LOG_SEQ_NBR 		NUMBER(9,0) NOT NULL ENABLE,
        SID 			NUMBER,
        STATISTIC# 		NUMBER,
        VALUE 			NUMBER,
         CONSTRAINT UT_PROCESS_STAT_PK PRIMARY KEY (UT_PROCESS_STATUS_ID, LOG_SEQ_NBR, STATISTIC#)
   ) organization index;

-- todo compress 2 on index
alter table ut_process_stat 
add constraint up_process_stat_fk 
              foreign key(ut_process_status_id, log_seq_nbr)
references ut_process_log(ut_process_status_id, log_seq_nbr);

