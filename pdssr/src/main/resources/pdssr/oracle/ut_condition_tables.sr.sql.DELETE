

create table ut_condition (
    ut_condition_id		        number(18) primary key,
    condition_name 		        varchar(30) not null,
    table_name		            varchar(60) not null,
    condition_msg	            varchar(256),
    sql_text                    varchar2(4000)  not null,
    narrative                   varchar2(409),
    condition_severity          numeric(1),
    condition_format_str        varchar2(256),
    corrective_action           varchar2(4000)
)
;--
 
create table ut_condition_run (
    ut_condition_run_id         number(18) primary key ,
    start_ts                    timestamp (6) not null
)
;--
create table ut_condition_run_parm (
    ut_condition_run_id         number(18) not null references ut_condition_run,
    parm_nm                     varchar(30) not null ,
    parm_type                   varchar(30) not null,
    parm_value_str              varchar(30),
    --constraint ut_rule_grp_run_parm_pk
    primary key (ut_condition_run_id, parm_nm)
)
;--
create table ut_condition_run_step (
    ut_condition_run_step_id    number(18) primary key,
    ut_condition_id             number(18) not null references ut_condition,
    ut_condition_run_id         number(18) not null references ut_condition_run,
    start_ts                    timestamp(6) not null,
    end_ts                      timestamp(6)
)
;--  
create table ut_condition_row_msg (
    ut_condition_run_step_id    number(18) references ut_condition_run_step,
    table_pk                    number(18),
    condition_msg               varchar(200),
    primary key (ut_condition_run_step_id, table_pk)
)
;--


