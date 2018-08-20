\set ECHO all
insert into product 
( 
   product_descr,
   mfr_id,
   mfr_product_id,
   case_gtin
)  
   select distinct 
       product_descr,
       17,
       substring(case_gtin from 6 for 8),
       case_gtin
   from etl_sale;

select count(*) from product;
