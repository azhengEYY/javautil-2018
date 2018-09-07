--@name create ut_condition_run_id_seq

create sequence ut_condition_run_id_seq;
--@name create ut_condition_id_seq
create sequence ut_condition_id_seq;
--@NAME create_ut_condition
create sequence ut_condition_run_step_id_seq;

create table ut_condition (  /* h2 and oracle */
    ut_condition_id		        number(9)  not null primary key,
    condition_name 		        varchar(30) not null,
    table_name		            varchar(60) not null,
    condition_msg	            varchar(2000),
    sql_text                    varchar(4000)  not null,
    narrative                   varchar(2000),
    condition_severity          numeric(1),
    condition_format_str        varchar(256),
    corrective_action           varchar(2000)
);
--@NAME create ut_condition_run
create table ut_condition_run (
    ut_condition_run_id         number(9)  not null primary key ,
    start_ts                    timestamp (6) not null
);
--@NAME create ut_condition_run_parm
create table ut_condition_run_parm (
    ut_condition_run_id         number(9) not null references ut_condition_run,
    parm_nm                     varchar(30) not null ,
    parm_type                   varchar(30) not null,
    parm_value_str              varchar(30),
    --constraint ut_rule_grp_run_parm_pk
    primary key (ut_condition_run_id, parm_nm)
);
--@NAME create ut_condition_run_step
create table ut_condition_run_step (
    ut_condition_run_step_id    number(9) not null primary key,
    ut_condition_id             integer not null references ut_condition,
    ut_condition_run_id         integer not null references ut_condition_run,
    start_ts                    timestamp(6) not null,
    end_ts                      timestamp(6)
);
--@NAME create ut_condition_row_msg
create table ut_condition_row_msg (
    ut_condition_run_step_id    number(9) not null references ut_condition_run_step,
    table_pk                    integer,
    condition_msg               varchar(200),
    primary key (ut_condition_run_step_id, table_pk)
);
