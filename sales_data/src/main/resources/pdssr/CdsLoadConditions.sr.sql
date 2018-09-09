name: ut_table_row_msg_delete
	sql: >
		delete from ut_table_row_msg
		where table_name = 'ETL_SALE'
		    and primary_key in (
		       select etl_sale_id
		       from etl_sale
		       where etl_file_id = %(ETL_FILE_ID)s
		)
name: ut_tab	
delete from ut_table_row_msg
where table_name = 'ETL_CUSTOMER'
     and primary_key in (select etl_customer_id
     from etl_customer
     where etl_file_id = %(ETL_FILE_ID)s
;--
delete from ut_table_row_msg 
where table_name = 'ETL_FILE' 
    and primary_key = %(ETL_FILE_ID)s
