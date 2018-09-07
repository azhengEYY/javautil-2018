set echo on
create or replace package test_dblogging as
    procedure  java_simulate;
end;
/
show errors; 

create or replace package body test_dblogging as

    procedure initialize is
    begin
        dbms_output.put_line('initialize connection');
        logger.prepare_connection;  -- stored procedure
    end initialize;

    procedure begin_job is
        my_job_log_id number;
    begin
        dbms_output.put_line('begin job');
        my_job_log_id := logger.begin_java_job(
 		P_PROCESS_NAME => 'Daemon or website',
 		P_CLASS_NAME   => 'getClass().getCanonicalName()',
 		P_MODULE_NAME  => 'test_dblogging',
 		P_STATUS_MSG   => 'Just some text',
 		P_THREAD_NAME  => 'Thread.currentThread().getName()'
        );
       -- 
    end begin_job;

    procedure do_action_1 is
        -- of course one would just select count but this is an exercise
	cursor utc_cursor is 
        select column_name 
        from user_tab_columns
	where nullable = 'YES';

        utc_rec utc_cursor%rowtype;
        nullable_count number;
    begin
        for utc_rec in utc_cursor
        loop
           nullable_count := nullable_count + 1;
        end loop;
         
    end do_action_1;


    procedure do_action_2 is
    begin
	dbms_output.put_line('do_action_2');
    end do_action_2;

    
    procedure do_action_3 is
    begin
	dbms_output.put_line('do_action_3');
    end do_action_3;
   
    procedure end_job is
    begin
        dbms_output.put_line('end_job');
    end end_job;

    procedure java_simulate is
    begin
        logger.prepare_connection;
        begin_job;
	logger.set_trace(12);
	logger.set_action('wtf');
        do_action_1;
        do_action_2;
        do_action_3;
	end_job;
    end;
end test_dblogging;
/
show errors; 
select spid from my_session_process;
exit;
