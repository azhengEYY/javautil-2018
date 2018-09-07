create sequence job_log_id_seq;

create table job_log (    
    job_log_id number(9),
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
    check ( ignore_flg in ('Y', 'N')) ,
    constraint job_log_pk primary key (job_log_id)
   ); -- end;


  create table job_msg
   (    job_msg_id 	  number(9),
        job_log_id 	  number(9),
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
         constraint job_msg_pk primary key (job_log_id, log_seq_nbr)
  );  --end job_msg;

create sequence job_step_id_seq;

 create table job_step (    
        job_step_id      number(9),
        job_log_id 	number(9),
	step_name               varchar(64),
	classname               varchar(256),
	step_info               varchar(2000),
        start_ts    		timestamp(9),
        end_ts  		timestamp(9),
        dbstats                 clob,
        step_info_json          clob,
         constraint job_step_pk primary key (job_step_id),
         constraint job_step_job_log_fk
	    foreign key (job_log_id) references job_log
  );

alter table job_msg 
add constraint upl_ups_fk 
foreign key (job_log_id) 
references job_log(job_log_id);

create or replace view job_step_vw as
select
        job_step_id,
        job_log_id,
	step_name,
	classname ,
	step_info,
        start_ts,
        end_ts ,
        timestampdiff('MS',start_ts,end_ts) elapsed_millis
from job_step;

create or replace view job_log_vw as 
select  
   job_log_id, 
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
   timestampdiff('MS',status_ts,end_ts) elapsed_millis 
from job_log;
