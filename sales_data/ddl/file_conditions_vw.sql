create or replace view load_conditions as
select  cond.table_name,
        cond.condition_name,
	count(row.ut_condition_run_step_id) condition_count
from    ut_condition_row_msg row,
	ut_condition_run_step step,
	ut_condition_run run,
	ut_condition cond
where row.ut_condition_run_step_id  = step.ut_condition_run_step_id and
      -- step.ut_condition_run_id = run.ut_condition_run_id`and
      step.ut_condition_id = cond.ut_condition_id
group by cond.table_name,
         cond.condition_name
;--
