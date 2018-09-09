select ut_condition_run_id 
from ut_condition_run
order by ut_condition_run_id;

select  ut_condition_run_id, to_number(parm_value_str,'99') etl_file_id
from ut_condition_run_parm
where parm_nm = 'ETL_FILE_ID'
order by to_number(parm_value_str,'99') ;

select  ut_condition_run_id, count(*) 
from    ut_condition_run_step
group by ut_condition_run_id
order by ut_condition_run_id;

select ut_condition_run_id, ut_condition_run_step_id  
from ut_condition_run_step
order by ut_condition_run_id, ut_condition_run_step;


select ut_condition_run_step_id, count(*) 
from ut_condition_row_msg
group by ut_condition_run_step_id
order by ut_condition_run_step_id;


select ut_condition_run_step_id  
from ut_condition_run_step 
where ut_condition_run_id = 571;

select count(*) from ut_condition_run;

select count(*) from ut_condition_run_parm;

select count(*) from ut_condition_run_step;

select count(*) from ut_condition_row_msg;


select count(*) 
from    ut_condition_run run,
        ut_condition_run_step step,
        ut_condition cond,
        ut_condition_run_parm p,
	-- ut_condition_row_msg row,
where 	
       	run.ut_condition_run_id = step.ut_condition_run_id and
       	step.ut_condition_id = cond.ut_condition_id and
    	run.ut_condition_run_id = p.ut_condition_run_id and
    	p.parm_nm= 'ETL_FILE_ID'
	--row.ut_condition_run_step_id  = step.ut_condition_run_step_id and
--group by cond.table_name,
    	--cond.condition_name,
    	--p.parm_value_str
--order by to_number(p.parm_value_str,'999999999'),
		--cond.table_name,
		cond.condition_name
