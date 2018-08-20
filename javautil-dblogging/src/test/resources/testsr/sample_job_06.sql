--#<
set serveroutput on
--#>
begin
	logger.begin_job('sample_job_01');
	logger.info($$PLSQL_UNIT,$$PLSQL_LINE,'begin loop');
        -- all messages should go to log file
	logger.set_filter_level(5);
	dbms_output.put_line('entering loop');
	dbms_output.put_line('about to emit warn');
	logger.warning($$PLSQL_UNIT,$$PLSQL_LINE,'5 - warn' );
	logger.info($$PLSQL_UNIT,$$PLSQL_LINE,'5 - info');
	logger.fine($$PLSQL_UNIT,$$PLSQL_LINE,'5 - fine');
	logger.finer($$PLSQL_UNIT,$$PLSQL_LINE,'5 - finer');
	logger.set_filter_level(9);
	logger.warning($$PLSQL_UNIT,$$PLSQL_LINE,'9 - warn' );
	logger.info($$PLSQL_UNIT,$$PLSQL_LINE,'9 - info');
	logger.fine($$PLSQL_UNIT,$$PLSQL_LINE,'9 - fine');
	logger.finer($$PLSQL_UNIT,$$PLSQL_LINE,'9 - finer');
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
