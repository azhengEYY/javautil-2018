set timing on 
set echo on

with sel1 as 
(
select ec.etl_customer_id, ship_to_cust_id, ec.cust_nm, etl_file_id
from etl_customer ec
where ec.etl_file_id = 1 and not exists (
    select 'x'
    from etl_sale
    where etl_sale.etl_file_id = 1 and
    ec.ship_to_cust_id = etl_sale.ship_to_cust_id
)
)
select count(*) from sel1;

with sel2 as 
(
select ec.etl_customer_id, ship_to_cust_id, ec.cust_nm, etl_file_id
from etl_customer ec
where ec.ship_to_cust_id in (
select ship_to_cust_id from etl_customer where etl_file_id = 1
minus
select distinct(ship_to_cust_id) from etl_sale where etl_file_id = 1
)
)
select count(*) from sel2;
