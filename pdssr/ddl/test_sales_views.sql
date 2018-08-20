select count(*)_ from customer_vw;

select * from effective_date_vw;

select tot_extended_net_amt from tot_sales_12_vw;

select sum(sum_extended_net_amt) from customer_product_last_12_months_vw;

select sum(sum_ext), count(*) from product_rank_12_vw;

select count(*) from product_undersold_by_customer_vw;

select count(*) from product_vw;


select etl_sale.case_gtin,
       count(*) invoice_count
from   etl_sale,
