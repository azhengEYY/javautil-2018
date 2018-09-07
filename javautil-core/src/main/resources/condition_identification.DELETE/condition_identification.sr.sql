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
