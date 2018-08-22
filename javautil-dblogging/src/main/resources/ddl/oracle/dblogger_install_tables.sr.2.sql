create sequence ut_process_status_id_seq;
create sequence ut_process_log_id_seq;

create table ut_process_status (    
    ut_process_status_id number(9),
    schema_name          varchar(30),
    process_name         varchar(128),
    thread_name          varchar(128),
    process_run_nbr      number(9,0),
    status_msg           varchar(256),
    status_id 	         varchar(1),
    status_ts 	         timestamp (9),
    end_ts               timestamp(9),
    sid                  number,
    serial_nbr           number,
    ignore_flg           varchar(1) default 'N',
    module_name          varchar(64),
    classname            varchar(255),
    tracefile_name       varchar(4000),
    tracefile_data       clob,
    tracefile_json       clob,
    abort_stacktrace     clob,
    check ( ignore_flg in ('Y', 'N')) ,
    constraint ut_process_status_pk primary key (ut_process_status_id)
   ); 


  create table ut_process_log
   (    ut_process_log_id 	  number(9),
        ut_process_status_id 	  number(9),
        log_msg_id 		  varchar2(8),
        log_msg 		  varchar2(256),
        log_msg_clob 		  clob,
        log_msg_ts 		  timestamp (9),
        elapsed_time_milliseconds number(9),
        log_seq_nbr 		  number(18,0) not null,
        caller_name 		  varchar2(100),
        line_nbr 		  number(5,0),
        call_stack 		  clob,
        log_level 		  number(2,0),
         constraint ut_process_log_pk primary key (ut_process_status_id, log_seq_nbr)
  ); 

create sequence ut_process_step_id_seq;

 create table ut_process_step (    
        ut_process_step_id      number(9),
        ut_process_status_id 	number(9),
	step_name               varchar(64),
	classname               varchar(256),
	step_info               varchar(2000),
        start_ts    		timestamp(9),
        end_ts  		timestamp(9),
        dbstats                 clob,
        step_info_json          clob,
         constraint ut_process_step_pk primary key (ut_process_step_id),
         constraint step_status_fk
	    foreign key (ut_process_status_id) references ut_process_status
  );

alter table ut_process_log 
add constraint upl_ups_fk 
foreign key (ut_process_status_id) 
references ut_process_status(ut_process_status_id);

create or replace view ut_process_step_vw as
select
        ut_process_step_id,
        ut_process_status_id,
	step_name,
	classname ,
	step_info,
        start_ts,
        end_ts ,
        end_ts - start_ts elapsed_millis
from ut_process_step;

create or replace view ut_process_status_vw as 
select  
   ut_process_status_id, 
   schema_name,         
   process_name,        
   thread_name,         
   process_run_nbr,     
   status_msg,          
   status_id,                         
   status_ts,                        
   end_ts,                               
   sid,                                      
   serial_nbr,                       
   ignore_flg,    
   module_name,    
   classname,             
   tracefile_name,                 
   end_ts - status_ts elapsed_millis 
from ut_process_status;
