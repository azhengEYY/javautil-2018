create or replace view 
customers as 
select distinct ship_to_cust_id 
from etl_sale;


select customers.ship_to_cust_id,
       top_products.case_gtin
from customers,
     top_products
where not exists
  ( select 'x' from etl_sale, top_products
    where
        etl_sale.ship_to_cust_id = customers.ship_to_cust_id  and
        etl_sale.case_gtin = top_products.case_gtin
  );

select customers.ship_to_cust_id, 
       top_products.case_gtin
from customers,
     top_products 
where not exists
  ( select 'x' from etl_sale, top_products
    where
        etl_sale.ship_to_cust_id = customers.ship_to_cust_id  and
        etl_sale.case_gtin = top_products.case_gtin
  )
order by customers.ship_to_cust_id,
         top_products.case_gtin;

create or replace view effective_date as
select max(invoice_dt) report_date from etl_sale;

create or replace view customer_product_last_12_months as
select  etl_sale.ship_to_cust_id, 
	case_gtin,
	sum(extended_net_amt) sum_extended_net_amt
from    etl_sale,
        effective_date    
where   invoice_dt  > effective_date.report_date - interval '1 year'
group by ship_to_cust_id,
         case_gtin
order by sum(extended_net_amt) desc;



create or replace view top_products as  
select case_gtin, sum(extended_net_amt) sum_ext
from etl_sale,
     effective_date
where invoice_dt  > effective_date.report_date - interval '1 year'
                group by case_gtin
                order by sum(extended_net_amt) desc
                limit 20 ;

create or replace view product_rank_12_vw as
    select etl_sale.case_gtin, sum(etl_sale.extended_net_amt) sum_ext,
           (sum(etl_sale.extended_net_amt) * 100 / tot_sales_12.tot_extended_net_amt) pct_product
    from etl_sale,
         tot_sales_12,
         effective_date
    where invoice_dt  > effective_date.report_date - interval '1 year'
                    group by case_gtin,
                    tot_sales_12.tot_extended_net_amt
    order by sum(extended_net_amt) desc
            ;

