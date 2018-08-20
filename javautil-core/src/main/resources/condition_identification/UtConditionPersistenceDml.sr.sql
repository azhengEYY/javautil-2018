--@name ut_condition_run_insert
insert into ut_condition_run (
          ut_condition_run_id, start_ts
) values ( 
         :ut_condition_run_id, :start_ts
);
--@name ut_condition_select
select ut_condition_id
from ut_condition
where
    condition_name          =         :rule_name and
    table_name              =         :table_name and
    condition_msg           =         :msg and
    sql_text                =         :sql_text and
    (narrative              =         :narrative or
    narrative is null and :narrative is null) and
    condition_severity      =         :severity and
    condition_format_str    =         :format_str;
--@name ut_condition_insert
insert into ut_condition (
    ut_condition_id,
    condition_name,
    table_name,
    condition_msg,
    sql_text,
    narrative,
    condition_severity,
    condition_format_str
) values (
    :ut_condition_id,
    :rule_name,
    :table_name,
    :msg,
    :sql_text,
    :narrative,
    :severity,
    :format_str
); 
--@name ut_condition_run_parm_insert
insert into ut_condition_run_parm (
    ut_condition_run_id, parm_nm, parm_type, parm_value_str
) values (
    :UT_CONDITION_RUN_ID, :PARM_NM, :PARM_TYPE, :PARM_VALUE
);
--@name ut_condition_run_step_insert
insert into ut_condition_run_step (
    ut_condition_run_step_id,
    ut_condition_id,
    ut_condition_run_id,
    start_ts
) values (
    :UT_CONDITION_RUN_STEP_ID,
    :UT_CONDITION_ID,
    :UT_CONDITION_RUN_ID,
    :START_TS
);
--@name ut_condition_row_msg_insert       
insert into ut_condition_row_msg (
    ut_condition_run_step_id, table_pk, condition_msg
) values (
    :UT_CONDITION_RUN_STEP_ID, :PRIMARY_KEY, :MSG
);
--@name deleteRow
delete from ut_condition_row_msg
where ut_condition_run_step_id in (
	select ut_condition_run_step_id 
	from ut_condition_run_step 
	where ut_condition_run_id = :ut_condition_run_id
);
--@name deleteStep
delete from ut_condition_run_step 
where ut_condition_run_id = :ut_condition_run_id;
--@name deleteParm
delete from ut_condition_run_parm 
where ut_condition_run_id = :ut_condition_run_id;
--@name deleteRun
delete from ut_condition_run 
where ut_condition_run_id = :ut_condition_run_id;
