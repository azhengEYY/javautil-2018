--@name job_log_insert
insert into job_log (    
	job_log_id, process_name, thread_name,
	status_msg, status_id, status_ts, tracefile_name,
	classname
) values (
	:job_log_id,       :process_name, :thread_name,
        :status_msg,  'A',           :status_ts, :tracefile_name,
	:classname
);
--@name job_step_insert
insert into job_step (
        job_step_id,   job_log_id, step_name, step_info, 
        classname,            start_ts
) values (
        :job_step_id, :job_log_id, :step_name,:step_info, 
        :classname,           :start_ts
);
--@name end_step
update job_step 
set end_ts = :end_ts
where job_step_id = :job_step_id;
--@name job_msg_insert
insert into job_msg (
	job_msg_id,  job_log_id, log_msg_id,                log_msg,
	log_msg_clob,       log_msg_ts,           elapsed_time_MILLISECONDS, log_seq_nbr,
	caller_name,        line_nbr,             call_stack,                log_level
) values (
	:job_msg_id, :job_log_id, :log_msg_id,                :log_msg,
	:log_msg_clob,      :log_msg_ts,           :elapsed_time_milliseconds, :log_seq_nbr,
	:caller_name,       :line_nbr,             :call_stack,                :log_level
);
--@name select_job_log_by_id
select * from job_log
where job_log_id = :job_log_id;
--@name abort_job
UPDATE job_log
SET 
    status_msg = 'ABORT',
    status_id = 'I',
    end_ts = :end_ts
where job_log_id = :job_log_id;
--@name end_job
UPDATE job_log
SET   
       status_msg = 'DONE',
       status_id = 'C',
       end_ts    = :end_ts
where job_log_id = :job_log_id;
