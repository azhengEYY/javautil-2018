create view etl_cust_product_month_view as
select
 distrib_id,
 mfr_id,
 mfr_product_id,
 ship_to_cust_id,
 product_descr,
 to_char(ship_dt, 'YYYY-MM'),
 sum(extended_net_amt) sum_extended_net_amt,
 distrib_product_ref,
 sum(cases_shipped) sum_cases_shipped,
 sum(boxes_shipped) sum_boxes_shipped,
 sum(units_shipped) sum_units_shipped,
 case_gtin,
 product_id
from etl_sale
group by
 distrib_id,
 mfr_id,
 mfr_product_id,
 ship_to_cust_id,
 product_descr,
 to_char(ship_dt, 'YYYY-MM'),
 distrib_product_ref,
 case_gtin,
 product_id

