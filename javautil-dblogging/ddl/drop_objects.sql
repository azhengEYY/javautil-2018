set pagesize 0
spool drop_objects_script.sql
select 'drop ' || object_type || ' ' || object_name ||  ';' from user_objects where object_type in ('SEQUENCE','VIEW','PACKAGE', 'PACKAGE BODY');
select 'drop table ' ||  object_name || ' cascade;' from user_objects where object_type in ('TABLE');
