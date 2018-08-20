set serveroutput on
declare 
	trace_filename varchar(4000);
	job_nbr number;
begin
	job_nbr := logger.begin_java_job(p_process_name => 'sample_job_01',
			      p_class_name => 'classname',
			      p_module_name => 'test',
                              p_status_msg => 'wtf',
			      p_thread_name => null);
	trace_filename  := logger.get_my_tracefile_name;
	dbms_application_info.set_action('showup');
	dbms_application_info.set_action('showup1');
	dbms_application_info.set_action('showup2');
	dbms_output.put_line('trace file: ' || trace_filename);
	logger.end_job;
exception when others
then
	logger.severe($$PLSQL_UINIT,$$PLSQL_LINE,sqlerrm);
	logger.abort_job;
raise;
end;
/
select spid from my_session_process;
