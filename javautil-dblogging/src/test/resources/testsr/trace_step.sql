declare
    cursor wank is select * from all_tab_columns;
    wank_rec wank%rowtype;
    count number := 0;
begin
  dbms_application_info.set_action('begin');
  logger.trace_step('wtf');
  FOR wank_rec in 
   LOOP
      count = count + 1;
   END LOOP;`
  dbms_application_info.set_action('end');
end;
/

select * from my_session_info;

