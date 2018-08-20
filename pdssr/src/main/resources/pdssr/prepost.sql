-
    with rec_count as (
    update etl_sale
    set product_id = (
        select  product_id
        from    product
        where   product.case_gtin = etl_sale.case_gtin
                or product.mfr_product_id = etl_sale.mfr_product_id)
    where   etl_file_id = %(ETL_FILE_ID)s
            and
            product_id is null
         and exists
    (
        select 'x' from product
        where   product.case_gtin = etl_sale.case_gtin
                or product.mfr_product_id = etl_sale.mfr_product_id
    )
    returning 1
    ) select count(*) from rec_count;
-
    with rec_count as (
    update etl_sale
    set org_mfr_id = (
        select  org_id
        from    org_mfr
        where   etl_sale.mfr_id = org_mfr.cds_mfr_id
    )
    where   etl_file_id = %(ETL_FILE_ID)s and
            org_mfr_id is null
         and exists (
            select 'x' from org_mfr
            where   etl_sale.mfr_id = org_mfr.cds_mfr_id
         )
    returning 1
    ) select count(*) from rec_count;

    with rec_count as (
    update etl_sale
    set org_distrib_id = (
        select  org_id
        from    org_distrib
        where   etl_sale.distrib_id = org_distrib.distrib_id
    )
    where   etl_file_id = %(ETL_FILE_ID)s and
            org_distrib_id is null
         and exists (
            select 'x' from org_distrib
            where   etl_sale.distrib_id = org_distrib.distrib_id
         )
    returning 1
    ) select count(*) from rec_count;

    with rec_count as (
      insert into distributor_customer(org_distrib_id, ship_to_cust_id)
      select distinct od.org_id,
             sale.ship_to_cust_id
      from   etl_sale sale,
             org_distrib od
      where  od.distrib_id = sale.distrib_id
      except
             select org_distrib_id,  ship_to_cust_id from distributor_customer
          returning 1
          ) select count(*) from rec_count;

    with rec_count as (
      update etl_sale
      set distributor_customer_id = 
            ( 
               select distributor_customer_id
               from distributor_customer dc,
                    org_distrib od
               where od.distrib_id = etl_sale.distrib_id and 
                     od.org_id = dc.org_distrib_id and
                     etl_sale.ship_to_cust_id = dc.ship_to_cust_id
             )
      where  etl_sale.distributor_customer_id is null 
            -- and etl_sale.etl_file_id = %(ETL_FILE_ID)s
          returning 1
          ) select count(*) from rec_count;
   
    
