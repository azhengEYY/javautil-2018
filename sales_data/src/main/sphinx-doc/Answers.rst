Answers to Questions you should have asked
==========================================

We do the work of finding data that is interesting and provide email notices 
that these reports exist.  Anyone subscribing to that event can get an email summary
notification and click directly to the report, only having to login.  We can support
gmail or facebook authentication so the user doesn't need another username and password.

It couldn't be easier.

Answers phone friendy format or downloadable in a variety of formats. 

Examples
&&&&&&&&

* Which operators are not selling recently introduced products

* Which operators have a sub-optimal mix of the top selling items by category

* Product declining sales for a vending operator

* Dropped products for a vending operator

* Parasitic promotion effect (Items not on promotion having declining sales during a promotion
as slot contention has had a parasitic effect.


Sales Analysis
==============

Missing Product
---------------

Objective
*********

List customers and the products they are should be selling but are not.

Approach
********

* Rank Products
* 

Customers
*********
As we are dealing with CDS reporting format and the scope of creation of customers in posting 
is beyond the scope of this section I will create a customer with a simple database view.

code::

        create or replace view
        customer_vw as
        select distinct ship_to_cust_id
        from etl_sale;

Effective Date
**************
In order for these queries to work without recreating the database we assume
an *effective date* which is the date of the last reported sale.

code::
        create or replace view effective_date as
        select max(invoice_dt) report_date from etl_sale;

Total Sales
***********

Total $ amount of sales for the preceeding 12 months based on last invoice date.

code::

        create or replace view tot_sales_12 as
        select sum(extended_net_amt) tot_extended_net_amt
        from etl_sale,
             effective_date
        where invoice_dt  > effective_date.report_date - interval '1 year';

Customer & Product Sales Last 12 months
***************************************

code::

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



Product Rank
************
There are many ways to rank product.

Units, gross revenue, profit, turns...

This is based simply on gross revenue

code::

    create or replace view product_rank_12_vw as
    select etl_sale.case_gtin, sum(etl_sale.extended_net_amt) sum_ext,
           sum(etl_sale.extended_net_amt) * 100 / tot_sales_12.tot_extended_net_amt
    from etl_sale,
         tot_sales_12
         effective_date
    where invoice_dt  > effective_date.report_date - interval '1 year'
                    group by case_gtin
                    order by sum(extended_net_amt) desc
            ;
    
    
Product Not Sold
================ 
What products are customers not selling?

code::

    create or replace view product_undersold_by_customer_vw as
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
             case_gtin;
   
Potential Revenue
-----------------

Now let us consider the amount that could be sold if these customers were to sell
your standard ratio of product.

We do that by multiplying the ratio to the dollar total for the customer.

In the best case scenario, the vending operator will add a slot for the product, replacing
with a product you don't distribute to him.  There is a distinct possibility that the sale
will be slightly parasitic and a lower volume product you do distribute will be replaced.

 
