declare
  g_file_handle utl_file.file_type := UTL_FILE.fopen ('UT_PROCESS_LOG_DIR', 'test_file', 'A');
begin
  null;
end;
/
select * from all_directories;



