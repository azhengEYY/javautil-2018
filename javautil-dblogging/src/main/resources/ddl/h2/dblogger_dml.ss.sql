--@name ut_process_status_insert
insert into ut_process_status (    
	ut_process_status_id, process_name, thread_name,
	status_msg, status_id, status_ts, tracefile_name,
	classname
) values (
	:ut_process_status_id,       :process_name, :thread_name,
        :status_msg,  'A',           :status_ts, :tracefile_name,
	:classname
);
--@name ut_process_step_insert
insert into ut_process_step (
        ut_process_step_id,   ut_process_status_id, step_name, step_info, 
        classname,            start_ts
) values (
        :ut_process_step_id, :ut_process_status_id, :step_name,:step_info, 
        :classname,           :start_ts
);
--@name end_step
update ut_process_step 
set end_ts = :end_ts
where ut_process_step_id = :ut_process_step_id;
--@name ut_process_log_insert
insert into ut_process_log (
	ut_process_log_id,  ut_process_status_id, log_msg_id,                log_msg,
	log_msg_clob,       log_msg_ts,           elapsed_time_MILLISECONDS, log_seq_nbr,
	caller_name,        line_nbr,             call_stack,                log_level
) values (
	:ut_process_log_id, :ut_process_status_id, :log_msg_id,                :log_msg,
	:log_msg_clob,      :log_msg_ts,           :elapsed_time_milliseconds, :log_seq_nbr,
	:caller_name,       :line_nbr,             :call_stack,                :log_level
);
--@name select_ut_process_status_by_id
select * from ut_process_status
where ut_process_status_id = :ut_process_status_id;
--@name abort_job
UPDATE ut_process_status
SET 
    status_msg = 'ABORT',
    status_id = 'I',
    end_ts = :end_ts
where ut_process_status_id = :ut_process_status_id;
--@name end_job
UPDATE ut_process_status
SET   
       status_msg = 'DONE',
       status_id = 'C',
       end_ts    = :end_ts
where ut_process_status_id = :ut_process_status_id;
