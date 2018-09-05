--#<
set echo on
drop table cursor_text;
drop table cursor_info;
drop table cursor_info_run;
drop table cursor_stat;
--#>

create sequence cursor_info_run_id_seq;
create sequence cursor_info_id_seq;
create sequence cursor_text_id_seq;
create sequence cursor_plan_id_seq;

 create table cursor_plan (
    cursor_plan_id     number(9) not null,
    explain_plan_hash  varchar(64) not null,
    explain_plan       clob not null
);

alter table cursor_plan 
add constraint cursor_plan_pk
primary key (cursor_plan_id);

create table cursor_info_run(
    cursor_info_run_id    number(9) not null,
    cursor_info_run_descr clob
);

alter table cursor_info_run 
add constraint cursor_info_run_id 
primary key (cursor_info_run_id);

create table cursor_text (
	cursor_text_id number(9) not null,
	sql_text_hash varchar (64),
	sql_text      clob
);

alter table cursor_text 
add constraint cursor_text_pk
primary key (cursor_text_id);

create table cursor_info (
	cursor_info_id          number(9) not null,
    cursor_info_run_id      number(9),
	cursor_text_id          number(9),
    parse_cpu_micros        number(9),
	parse_elapsed_micros    number(9),
	parse_blocks_read       number(9),
	parse_consistent_blocks number(9),
	parse_current_blocks    number(9),
	parse_lib_miss          number(9),
	parse_row_count         number(9),
    exec_cpu_micros         number(9),
	exec_elapsed_micros     number(9),
	exec_blocks_read        number(9),
	exec_consistent_blocks  number(9),
	exec_current_blocks     number(9),
	exec_lib_miss           number(9),
	exec_row_count          number(9),
    fetch_cpu_micros        number(9),
	fetch_elapsed_micros    number(9),
	fetch_blocks_read       number(9),
	fetch_consistent_blocks number(9),
	fetch_current_blocks    number(9),
	fetch_lib_miss          number(9),
	fetch_row_count         number(9),
	cursor_plan_id          number(9)
);


alter table cursor_info 
add constraint cursor_info_pk
primary key (cursor_info_id);

alter table cursor_info
add constraint cursor_info_text_fk
foreign key (cursor_text_id)
references cursor_text;

alter table cursor_info
add constraint cursor_info_plan_fk
foreign key (cursor_plan_id)
references cursor_plan;

alter table cursor_info add constraint 
cursor_info_run_fk foreign key (cursor_info_run_id)
references cursor_info_run;

create or replace view  cursor_info_vw as 
select 
	cursor_info.cursor_info_id,
    cursor_info.cursor_text_id,
    cursor_text.sql_text,
    parse_cpu_micros,
    parse_elapsed_micros,
    parse_blocks_read,
    parse_consistent_blocks,
    parse_current_blocks,
    parse_lib_miss,
    parse_row_count,
    exec_cpu_micros,
    exec_elapsed_micros,
    exec_blocks_read,
    exec_consistent_blocks,
    exec_current_blocks,
    exec_lib_miss,
    exec_row_count,
    fetch_cpu_micros,
    fetch_elapsed_micros,
    fetch_blocks_read,
    fetch_consistent_blocks,
    fetch_current_blocks,
    fetch_lib_miss,
    fetch_row_count,
    parse_cpu_micros + exec_cpu_micros + fetch_cpu_micros cpu_micros ,
    parse_elapsed_micros + exec_elapsed_micros + fetch_elapsed_micros elapsed_micros ,
    parse_blocks_read + exec_blocks_read + fetch_blocks_read blocks_read ,
    parse_consistent_blocks + exec_consistent_blocks + fetch_consistent_blocks consistent_blocks ,
    parse_current_blocks + exec_current_blocks + fetch_current_blocks current_blocks ,
    parse_lib_miss + exec_lib_miss + fetch_lib_miss lib_miss ,
    parse_row_count + exec_row_count + fetch_row_count row_count,
    explain_plan
from cursor_info,
     cursor_text,
     cursor_plan
 where cursor_info.cursor_text_id = cursor_text.cursor_text_id and
       cursor_info.cursor_plan_id = cursor_plan.cursor_plan_id;
    

create table cursor_stat (
    cursor_info_id   number(9) not null,
    seq_nbr          number(3) not null,
    operation_depth  number(2) not null, 
    operation        varchar(100),
    consistent_reads number(9),
    physical_reads   number(9),
    physical_writes  number(9),
    elapsed_millis   number(12)
);



alter table cursor_stat add 
constraint cursor_stat_pk 
primary key (cursor_info_id, seq_nbr);


