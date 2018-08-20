set echo on
set serveroutput on
declare 
	trace_filename varchar(4000);
	job_nbr number;
	cursor all_tab_columns_cur is select * from all_tab_columns;
        all_tab_columns_rec all_tab_columns_cur%rowtype;
        session_process_rec my_session_process%rowtype;
begin
	job_nbr := logger.begin_java_job(p_process_name => 'sample_job_01',
			      p_class_name => 'classname',
			      p_module_name => 'test',
                              p_status_msg => 'wtf',
			      p_thread_name => null);
	trace_filename  := logger.get_my_tracefile_name;
	logger.trace_step('showup dog');
	dbms_application_info.set_action('showup');
	for all_columns_rec in all_tab_columns_cur
        loop
           null;
	end loop;
	dbms_application_info.set_action('showup1');
	select * into session_process_rec from my_session_process;
	dbms_application_info.set_action('showup2');
	logger.trace_step('showup3');
	dbms_output.put_line('trace file: ' || trace_filename);
	logger.end_job;
exception when others
then
	logger.severe($$PLSQL_UINIT,$$PLSQL_LINE,sqlerrm);
	logger.abort_job;
raise;
end;
/
exec dbms_application_info.set_action('get some action');
select spid from my_session_process;
select tracefile from my_session_process;
