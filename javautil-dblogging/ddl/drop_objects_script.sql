drop SEQUENCE UT_PROCESS_LOG_ID_SEQ;                                            
drop VIEW UT_PROCESS_STEP_VW;                                                   
drop VIEW UT_PROCESS_STATUS_VW;                                                 
drop PACKAGE LOGGER;                                                            
drop PACKAGE BODY LOGGER;                                                       
drop table VALIDATED_ADDRESS cascade;                                           
drop table UT_PROCESS_STATUS cascade;                                           
drop table UT_PROCESS_LOG cascade;                                              
drop table UT_PROCESS_STEP cascade;                                             
drop table cursor_text cascade;
drop table cursor_explain_plan cascade;
drop table cursor_sql_text cascade;
drop table cursor_info cascade;
drop table cursor_info_run cascade;
drop sequence cursor_info_run_id_seq;
drop sequence cursor_info_id_seq;

