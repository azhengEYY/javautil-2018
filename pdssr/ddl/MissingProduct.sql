create table customer_mv 
(
	customer_id serial primary key,
        ship_to_cust_id  varchar(10)
);

insert into customer_mv
(
    ship_to_cust_id 
)
select
    distinct ship_to_cust_id
from
    etl_sale;

create table customer_product_12_mv
(
	ship_to_cust_id varchar(10),
	case_gtin       varchar(14),
	extended_net_amt numeric(14,2)
);

create table product_mv
(
	case_gtin varchar(14)
);

insert into product_mv
(
        case_gtin
)
select distinct case_gtin from etl_sale;

insert into customer_product_12_mv
(
    ship_to_cust_id,
    case_gtin,
    extended_net_amt
)
select etl_sale.ship_to_cust_id, 
                  case_gtin,
                  sum(extended_net_amt) 
          from    etl_sale,
                  effective_date_vw
          where   invoice_dt  > effective_date_vw.report_date - interval '1 year'
          group by ship_to_cust_id,
                  case_gtin
          order by sum(extended_net_amt) desc;

    create or replace view customer_vw as
    select distinct ship_to_cust_id
    from etl_sale;


select customer_product_12_mv.ship_to_cust_id,
        customer_product_12_mv.extended_net_amt,
        product_mv.case_gtin
    from
        product_mv
    left outer join  customer_product_12_mv  on
        customer_product_12_mv.case_gtin = product_mv.case_gtin
    order by product_mv.case_gtin;


with customer_product_outer_12_mv as (
select customer_product_12_mv.ship_to_cust_id,
        customer_product_12_mv.extended_net_amt,
        product_mv.case_gtin
    from
        product_mv
    left outer join  customer_product_12_mv  on
        customer_product_12_mv.case_gtin = product_mv.case_gtin
) 
select * from customer_product_outer_12_mv 
where ship_to_cust_cd is null
   and case_gtin = '00012345523142'

where case_gtin = 
    order by product_mv.case_gtin;


    create or replace view customer_product_invoice_vw as
    select etl_sale.ship_to_cust_id,
	count(*),
	sum(extended_net_amt) sum_extended_net_amt
    from
        etl_sale
    left join  product_vw on  
	etl_sale.case_gtin = product_vw.case_gtin
    group by etl_sale.ship_to_cust_id,
             etl_sale.case_gtin;



--
--Product
--*******
--code::
  create or replace view product_vw as
  select distinct case_gtin
  from etl_sale;
--
--Effective Date
--**************
--In order for these queries to work without recreating the database we assume
--an *effective date* which is the date of the last reported sale.
--
--code::
        create or replace view effective_date_vw as
        select max(invoice_dt) report_date from etl_sale;
--
--Total Sales
--***********
--
--Total $ amount of sales for the preceeding 12 months based on last invoice date.
--
--code::
--
          create or replace view tot_sales_12_vw as
          select sum(extended_net_amt) tot_extended_net_amt
          from etl_sale,
               effective_date_vw
          where invoice_dt  > effective_date_vw.report_date - interval '1 year';
  
--Customer & Product Sales Last 12 months
--***************************************
--
--code::
--
          create or replace view customer_product_last_12_months_vw as
          select  etl_sale.ship_to_cust_id, 
                  case_gtin,
                  sum(extended_net_amt) sum_extended_net_amt
          from    etl_sale,
                  effective_date_vw
          where   invoice_dt  > effective_date_vw.report_date - interval '1 year'
          group by ship_to_cust_id,
                  case_gtin
          order by sum(extended_net_amt) desc;
--
--
--
--Product Rank
--************
--There are many ways to rank product.
--
--Units, gross revenue, profit, turns...
--
--This is based simply on gross revenue
--
--code::
--


      create or replace view product_rank_12_vw as
      select etl_sale.case_gtin, 
		sum(etl_sale.extended_net_amt) sum_ext,
             sum(etl_sale.extended_net_amt) * 100 / tot_sales_12_vw.tot_extended_net_amt pct_product
      from etl_sale,
           tot_sales_12_vw,
           effective_date_vw
      where invoice_dt  > effective_date_vw.report_date - interval '1 year'
      group by case_gtin,
               tot_sales_12_vw.tot_extended_net_amt
      order by sum(extended_net_amt) desc;
--    
--    
--Product Not Sold
--================ 
--What products are customers not selling?
--
--code::

      create or replace view product_undersold_by_customer_vw as
      select customer_vw.ship_to_cust_id,
             product_rank_12_vw.case_gtin
      from customer_vw,
           product_rank_12_vw
      where not exists ( 
	 select 'x' from etl_sale, 
			product_rank_12_vw
          where
              etl_sale.ship_to_cust_id = customer_vw.ship_to_cust_id  and
              etl_sale.case_gtin = product_rank_12_vw.case_gtin
        )
      order by customer_vw.ship_to_cust_id,
               case_gtin;
--   
--Potential Revenue
-------------------
--
--Now let us consider the amount that could be sold if these customers were to sell
--your standard ratio of product.
--
--We do that by multiplying the ratio to the dollar total for the customer.
--
--In the best case scenario, the vending operator will add a slot for the product, replacing
--with a product you don't distribute to him.  There is a distinct possibility that the sale
--will be slightly parasitic and a lower volume product you do distribute will be replaced.
--
-- 

select every customer and product combination


create or replace view 
customer_product_vw as 
select cv.ship_to_cust_id,
       pv.case_gtin
from customer_vw cv,
	product_vw pv;

       
create or replace view 
customer_product_ext_net_vw as 
with cust_product as
(
	select cv.ship_to_cust_id,
       		pv.case_gtin
		from customer_vw cv,
		product_vw pv
)
select cust_product.ship_to_cust_id,
       cust_product.case_gtin,
       sum(extended_net_amt) sum_extended_net_amt,
       count(extended_net_amt) invoice_count
from cust_product,
     etl_sale
where cust_product.ship_to_cust_id = etl_sale.ship_to_cust_id and
      cust_product.case_gtin = etl_sale.case_gtin
group by 
	cust_product.ship_to_cust_id,
       cust_product.case_gtin;

create or replace view 
customer_noproduct_vw as 
with cust_product_all as
(
        select cv.ship_to_cust_id,
                pv.case_gtin
                from customer_vw cv,
                product_vw pv
),
cust_product_sold as
(
	select distinct
		etl_sale.ship_to_cust_id,
               etl_sale.case_gtin
       from etl_sale
e
select 
   cust_product_all.ship_to_cust_id, -- huh
   cust_product_all.case_gtin
from cust_product_all
except
select cust_product_sold.ship_to_cust_id,
       cust_product_sold.case_gtin
from
    cust_product_sold
order by
   ship_to_cust_id,
   case_gtin
;
    
select cust_product.ship_to_cust_id,
       cust_product.case_gtin,
       sum(extended_net_amt) sum_extended_net_amt,
       count(extended_net_amt) invoice_count
from cust_product,
     etl_sale
where cust_product.ship_to_cust_id = etl_sale.ship_to_cust_id and
      cust_product.case_gtin = etl_sale.case_gtin
group by 
        cust_product.ship_to_cust_id,
       cust_product.case_gtin;

