create or replace view product_vw as 
select 
  o.org_cd,
  o.org_nm,
  p.product_id,
  p.mfr_id,
  p.product_descr,
  p.mfr_product_id,
  p.case_gtin,
  p.box_gtin,
  p.unit_gtin,
  p.units_per_box,
  p.units_per_case
from  product p,
      org     o
where p.mfr_id = o.org_id
;--
