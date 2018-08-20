set serveroutput on
select count(*) from user_tab_columns;
select count(*) from user_tab_columns, user_tables;
select logger.get_my_tracefile from dual;
