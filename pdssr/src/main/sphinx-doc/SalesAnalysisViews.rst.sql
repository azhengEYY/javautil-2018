--Sales Analysis
--==============
--
--Missing Product
------------------------------
--
--Objective
--*********
--
--List customers and the products they are should be selling but are not.
--
--Approach
--********
--
--* Rank Products
--* 
--
--Customers
--*********
--As we are dealing with CDS reporting format and the scope of creation of customers in posting 
--is beyond the scope of this section I will create a customer with a simple database view.
--
--This will be replaced with an alternate view later in this course.
--
--code::
--
    create or replace view customer_vw as
    select distinct ship_to_cust_id
    from etl_sale;

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
