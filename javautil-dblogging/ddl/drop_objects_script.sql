drop SEQUENCE job_msg_ID_SEQ;                                            
drop VIEW job_step_VW;                                                   
drop VIEW job_log_VW;                                                 
drop PACKAGE LOGGER;                                                            
drop PACKAGE BODY LOGGER;                                                       
drop table VALIDATED_ADDRESS cascade;                                           
drop table job_log cascade;                                           
drop table job_msg cascade;                                              
drop table job_step cascade;                                             
drop table cursor_text cascade;
drop table cursor_explain_plan cascade;
drop table cursor_sql_text cascade;
drop table cursor_info cascade;
drop table cursor_info_run cascade;
drop sequence cursor_info_run_id_seq;
drop sequence cursor_info_id_seq;

