--#<
set serveroutput on
--#>
begin
	logger.begin_job('sample_job_01');
	logger.info($$PLSQL_UNIT,$$PLSQL_LINE,'begin loop');
        -- all messages should go to log file
	logger.set_filter_level(9);
	dbms_output.put_line('entering loop');
	for i in 1..2  
	loop
		dbms_output.put_line('about to emit into');
		logger.info($$PLSQL_UNIT,$$PLSQL_LINE,'i is ' || to_char(i));
		dbms_output.put_line('emitted info');
		logger.fine($$PLSQL_UNIT,$$PLSQL_LINE,'i is ' || to_char(i));
	end loop; 
	dbms_output.put_line('ending job');
	logger.end_job;
exception when others
then
	logger.severe($$PLSQL_UINIT,$$PLSQL_LINE,sqlerrm);
	logger.abort_job;
raise;
end;
--#<
/
--#>
