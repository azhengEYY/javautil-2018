--@NAME org_sql
select org_id from org where org_cd = :ORG_CD
;--
--@NAME etl_customer_insert
    insert into ETL_CUSTOMER (
      ETL_FILE_ID, LINE_NUMBER, SHIP_TO_CUST_ID, CUST_NM,
      ADDR_1, ADDR_2, CITY, STATE,
      POSTAL_CD, TEL_NBR, NATIONAL_ACCT_ID,
      SPECIAL_FLG
    ) values (
      :ETL_FILE_ID, :LINE_NUMBER, :SHIP_TO_CUST_ID, :CUST_NM,
      :ADDR_1, :ADDR_2, :CITY, :STATE,
      :POSTAL_CD, :TEL_NBR, :NATIONAL_ACCT_ID,
      :SPECIAL_FLG
    )
;--
--@NAME etl_file_initial_insert
insert into ETL_FILE ( RPT_ORG_ID, DATAFEED_ORG_ID)
select org_id, org_id
from org
where org_cd = :ORG_CD
;--
--@NAME org_select
select org_id from org where org_cd = :ORG_CD
;--
--@NAME etl_sale_tot_insert
     insert into etl_sale_tot (
         ETL_FILE_ID,    LINE_NUMBER,    SALES_START_DT, SALES_END_DT,
         FILE_CREATE_DT, SALES_REC_CNT, SUM_EXT_NET_AMT
     ) values (
         :ETL_FILE_ID,     :LINE_NUMBER,  :SALES_START_DT, :SALES_END_DT, 
         :FILE_CREATE_DT, :SALES_REC_CNT, :SUM_EXT_NET_AMT
     )
;--
--@NAME etl_inventory_insert
    insert into ETL_INVENTORY (
        ETL_FILE_ID,           LINE_NUMBER, DISTRIBUTOR_ID,        MFR_ID,
        MFR_PRODUCT_ID,        COMMENTS,    CASES,                 BOXES,
        UNITS,                 CASE_GTIN
    ) values (
        :ETL_FILE_ID,      :LINE_NUMBER,   :DISTRIBUTOR_ID,   :MFR_ID,
        :MFR_PRODUCT_ID,   :COMMENTS,      :CASES,            :BOXES,
        :UNITS,            :CASE_GTIN
    )
;--
--@NAME etl_inventory_tot_insert
    insert into ETL_INVENTORY_TOT (
        ETL_FILE_ID, line_number,
        INVENTORY_DT, FILE_CREATION_DT, RECORD_CNT_REPORTED
    ) values (
        :ETL_FILE_ID, :LINE_NUMBER,
        :INVENTORY_DT, :FILE_CREATION_DT, :RECORD_CNT_REPORTED
    )
;--
--@NAME etl_sale_insert
        insert into ETL_SALE (
            ETL_FILE_ID, 
            LINE_NUMBER,
            DISTRIB_ID, MFR_ID,
            MFR_PRODUCT_ID, SHIP_TO_CUST_ID, INVOICE_CD, INVOICE_DT,
            SHIP_DT, EXTENDED_NET_AMT,
            DISTRIB_PRODUCT_REF,
            PRODUCT_DESCR, CASES_SHIPPED, BOXES_SHIPPED, UNITS_SHIPPED,
            CASE_GTIN
        ) values (
            :ETL_FILE_ID, :LINE_NUMBER,
            :DISTRIB_ID, :MFR_ID,
            :MFR_PRODUCT_ID, :SHIP_TO_CUST_ID, :INVOICE_CD, :INVOICE_DT,
            :SHIP_DT, :EXTENDED_NET_AMT,
            :DISTRIB_PRODUCT_REF,
            :PRODUCT_DESCR, :CASES_SHIPPED, :BOXES_SHIPPED, :UNITS_SHIPPED,
            :CASE_GTIN
        )
;--
--@NAME etl_customer_tot_insert
    insert into etl_customer_tot (
         etl_file_id,       line_number,    customer_count
    ) values (
        :ETL_FILE_ID,  :LINE_NUMBER, :CUSTOMER_COUNT
    )
