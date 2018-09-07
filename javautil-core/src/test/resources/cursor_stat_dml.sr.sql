--@NAME cursor_info_insert
insert into cursor_info(
    cursor_info_id,   explain_plan_hash,    sql_text_hash,
    parse_cpu_micros, parse_elapsed_micros, parse_blocks_read, parse_consistent_blocks, parse_current_blocks, parse_row_count, 
    exec_cpu_micros,  exec_elapsed_micros,  exec_blocks_read,  exec_consistent_blocks,  exec_current_blocks,  exec_row_count,
    fetch_cpu_micros, fetch_elapsed_micros, fetch_blocks_read, fetch_consistent_blocks, fetch_current_blocks, fetch_row_count
) values (
    :cursor_info_id,   :explain_plan_hash,    :sql_text_hash,
    :parse_cpu_micros, :parse_elapsed_micros, :parse_blocks_read, :parse_consistent_blocks, :parse_current_blocks,   :parse_row_count, 
    :exec_cpu_micros,  :exec_elapsed_micros,  :exec_blocks_read,  :exec_consistent_blocks,  :exec_current_blocks,    :exec_row_count,
    :fetch_cpu_micros, :fetch_elapsed_micros, :fetch_blocks_read, :fetch_consistent_blocks, :fetch_current_blocks,   :fetch_row_count
);

--@name cursor_text_insert
insert into cursor_sql_text (
	sql_text_hash, sql_text
)
values (
	:sql_text_hash, :sql_text
);

--@name cursor_info_run_insert
insert into cursor_info_run(
    cursor_info_run_id, cursor_info_run_descr
) values (
    :cursor_info_run_id, :cursor_info_run_descr
);

--@name cursor_stat_insert
insert into cursor_stat (
    cursor_info_id, seq_nbr, operation_depth, operation,
    consistent_reads, physical_reads, physical_writes, elapsed_millis
 ) values (
     :cursor_info_id, :seq_nbr, :operation_depth, :operation,
    :consistent_reads, :physical_reads, :physical_writes, :elapsed_millis
    );
--@name cursor_plan_insert
insert into cursor_explain_plan (
    explain_plan_hash, explain_plan
) values (
    :explain_plan_hash, :explain_plan
);
--@name cursor_text_by_hash
select * 
from cursor_sql_text
where sql_text_hash = :sql_text_hash;
--@name cursor_plan_by_hash
select * 
from cursor_explain_plan
where explain_plan_hash = :explain_plan_hash;
--@name cursor_plan_by_id
select * 
from cursor_explain_plan
where cursor_plan_id = :cursor_plan_id;