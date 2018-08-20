rem run as sys
create view my_session_info as select * from 
v$session where audsid = userenv('sessionid');

grant select on my_session_info to public;

create public synonym my_session_info for sys.my_session_info;
