set serveroutput on
declare
     my_tracefile_name varchar2(4096) := logger.get_my_tracefile_name;
begin
     dbms_output.put_line(my_tracefile_name);
     dbms_output.put_line(logger.basename(my_tracefile_name));
end;
/
     
