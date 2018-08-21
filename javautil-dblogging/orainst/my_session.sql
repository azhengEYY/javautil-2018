set echo on
rem run as sys

alter session set container = sales_reporting_pdb;

drop view my_session;
drop public synonym my_session;
select * from dba_objects where object_name = 'MY_SESSION';
create or replace view my_session_vw as 
    select 
    s.SADDR, 
    s.SID, 
    s.SERIAL#, 
    s.AUDSID , 
    s.PADDR,
    s.USER#, 
    s.USERNAME, 
    s.COMMAND, 
    s.OWNERID,
    s.TADDR, 
    s.LOCKWAIT, 
    s.STATUS , 
    s.SERVER ,
    s.SCHEMA#, 
    s.SCHEMANAME, 
    s.OSUSER , 
    s.PROCESS,
    s.MACHINE, 
    s.PORT, 
    s.TERMINAL, 
    s.PROGRAM,
    s.TYPE, 
    	s.SQL_ADDRESS, 
    	s.SQL_HASH_VALUE, 
    	s.SQL_ID ,
    	s.SQL_CHILD_NUMBER, 
    	s.SQL_EXEC_START , 
    	s.SQL_EXEC_ID, 
    	s.PREV_SQL_ADDR,
    s.PREV_HASH_VALUE, 
    	s.PREV_SQL_ID, 
    	s.PREV_CHILD_NUMBER, 
    	s.PREV_EXEC_START,
    	s.PREV_EXEC_ID, 
    	s.PLSQL_ENTRY_OBJECT_ID, 
    	s.PLSQL_ENTRY_SUBPROGRAM_ID, 
    	s.PLSQL_OBJECT_ID,
    	s.PLSQL_SUBPROGRAM_ID, 
    	s.MODULE , 
    	s.MODULE_HASH, 
    	s.ACTION ,
    	s.ACTION_HASH, 
    	s.CLIENT_INFO,
    s.FIXED_TABLE_SEQUENCE,
    s.ROW_WAIT_OBJ#,
    s.ROW_WAIT_FILE# ,
    s.ROW_WAIT_BLOCK#,
    s.ROW_WAIT_ROW#,
    s.TOP_LEVEL_CALL#,
    s.LOGON_TIME,
    s.LAST_CALL_ET,
    s.PDML_ENABLED,
    s.FAILOVER_TYPE,
    s.FAILOVER_METHOD,
    s.FAILED_OVER,
    s.RESOURCE_CONSUMER_GROUP,
    s.PDML_STATUS,
    s.PDDL_STATUS,
    s.PQ_STATUS,
    s.CURRENT_QUEUE_DURATION ,
    s.CLIENT_IDENTIFIER,
    s.BLOCKING_SESSION_STATUS,
    s.BLOCKING_INSTANCE,
    s.BLOCKING_SESSION,
    s.FINAL_BLOCKING_SESSION_STATUS,
    s.FINAL_BLOCKING_INSTANCE,
    s.FINAL_BLOCKING_SESSION ,
    s.SEQ#,
    s.EVENT# ,
    s.EVENT,
    s.P1TEXT ,
    s.P1,
    s.P1RAW,
    s.P2TEXT ,
    s.P2,
    s.P2RAW,
    s.P3TEXT ,
    s.P3,
    s.P3RAW,
    s.WAIT_CLASS_ID,
    s.WAIT_CLASS#,
    s.WAIT_CLASS,
    s.WAIT_TIME,
    s.SECONDS_IN_WAIT,
    s.STATE,
    s.WAIT_TIME_MICRO,
    s.TIME_REMAINING_MICRO,
    s.TIME_SINCE_LAST_WAIT_MICRO,
    s.SERVICE_NAME,
    s.SQL_TRACE,
    s.SQL_TRACE_WAITS,
    s.SQL_TRACE_BINDS,
    s.SQL_TRACE_PLAN_STATS
from    sys.v$session s
where s.audsid = userenv('sessionid');

grant select on sys.v_$process to public;
--grant select on v$process to public;
grant select on sys.v_$session to public;
--grant select on v$session to public;

grant select on my_session_vw to public;

drop public synonym my_session;
create public synonym my_session for my_session_vw;

select * from my_session;
