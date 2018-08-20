create view sale_product_cust_vw as
select
 s.etl_sale_id,
 s.org_distrib_id,
 s.org_mfr_id,
 s.product_id,
 s.distributor_customer_id,
 s.inv_amt,
 s.case_equiv_qty,
 s.item_qty,
 s.inv_dt,
 p.product_descr
from post_sale s,
     product p,
     distributor_customer c
where p.product_id = s.product_id and
	c.distributor_customer_id = s.distributor_customer_id;
