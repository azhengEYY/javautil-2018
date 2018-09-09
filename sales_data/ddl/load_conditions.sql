create view load_conditions as
select  
	p.parm_value_str etl_file_id,
	cond.table_name,
        cond.condition_name,
	count(row.ut_condition_run_step_id) condition_count
from    ut_condition_row_msg row,
	ut_condition_run_step step,
	ut_condition_run run,
	ut_condition cond,
	ut_condition_run_parm p

where row.ut_condition_run_step_id  = step.ut_condition_run_step_id and
       step.ut_condition_run_id = run.ut_condition_run_id and
      step.ut_condition_id = cond.ut_condition_id and
      p.ut_condition_run_id = run.ut_condition_run_id and
      p.parm_nm= 'ETL_FILE_ID'q
group by cond.table_name,
         cond.condition_name,
	p.parm_value_str
order by p.parm_value_str, cond.table_name, cond.condition_name 
;--
