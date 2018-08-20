set echo on 
alter session set container = sales_reporting_pdb;

create or replace view my_open_cursor as
select 
 SADDR,
 SID,
 USER_NAME,
 ADDRESS,
 HASH_VALUE,
 SQL_ID ,
 SQL_TEXT,
 LAST_SQL_ACTIVE_TIME,
 SQL_EXEC_ID,
 CURSOR_TYPE,
 CHILD_ADDRESS,
 CON_ID 
from v$open_cursor 
where sid = (select sid from v$mystat where rownum = 1);

grant select on my_open_cursor to public;

create public synonym my_open_cursor for sys.my_open_cursor;

create or replace view my_duplicate_cursor as
select sql_text, count(*) sql_count
from my_open_cursor 
group by sql_text 
having count(*) > 1; 

grant select on my_duplicate_cursor to public;

create or replace public synonym my_duplicate_cursor for sys.my_open_cursor;

describe my_duplicate_cursor;

select owner, object_type from dba_objects where object_name = 'MY_OPEN_CURSOR';

