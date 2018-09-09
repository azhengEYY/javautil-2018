Processing
==========

Initialize
----------

Data Load
---------

The data files are parsed and the data is loaded into a relational database
with one table per record type, sufficient information is retained that
an identical output file can be created for an input file.

This allows the data file to accessed using SQL and provides atomic operations.

A file is completely loaded or is rejected and not loaded at all.

Being completely loaded does not mean that the file is satisfactory.

Relational Databases supported are

* Oracle
* postgress
* sqlite

Data Format
&&&&&&&&&&&

Exception Identification
------------------------

The exception identification is performed by *javautil.org* program, condition identification.


This code was developed by Trinity Technical Services (My company) for 
importing data from other systems into Diamond Distribution but is extremely
generic.  

There are several versions of this code, the one employed at Custom Data 
Services at the time of my departure was based on rules stored in the database.

While that version and subsequent improved versions could be used, I recommend
the latest version that stores all the rules in a flat file.

Prior to use of the exception identification system Custom Data Solutions 
ran stacks of reports that were hand examined.  It was a slow and inflexible process.

The new system allowed for acceptance of conditions and partial posting while 
suspect records were corrected.  

Examples of Conditions
**********************
- Sales with no associated customer
- Products not on product master
- Suspicious quantity
- Suspicious price

Rule Examples
&&&&&&&&&&&&&

.. code-block:: sql

    -   rule_name:  NO_CUSTOMER_TOTAL
        table_name: ETL_FILE
        msg:      No customer total record
        sql_text: >
                select etl_file_id
                from etl_file
                where etl_file_id = %(ETL_FILE_ID)s
                and not exists (
                   select 'x'
                   from etl_customer_tot
                   where etl_file_id = %(ETL_FILE_ID)s
                )
        narrative: >
                There is no customer total record
        severity: 3
        format_str: "No customer total record in %s"
        java_format: "No customer total record for etl_file_id %s"
    
    -   rule_name:  CUSTOMERS_BUT_NO_TOTAL
        table_name: ETL_FILE
        msg:      Customers / No customer total
        sql_text: >
                select etl_file_id
                from etl_file
                where etl_file_id = %(ETL_FILE_ID)s
                and not exists (
                   select 'x'
                   from etl_customer_tot
                   where etl_file_id = %(ETL_FILE_ID)s
                ) and exists (
                   select 'x'
                   from etl_customer
                   where etl_file_id = %(ETL_FILE_ID)s
                )     
        narrative: >
                There is no customer total record but customers were reported
                It is impossible to determine the effective date of the 
                customer information without this record
        severity: 3
        format_str: "No customer total record in %s"
        java_format: "No customer total record for etl_file_id %s"
    
    -   rule_name: NO_SALES_FOR_CUSTOMER
        table_name: ETL_CUSTOMER
        msg:      No sales for customer
        sql_text: >
                select ec.etl_customer_id, ship_to_cust_id, ec.cust_nm, etl_file_id
                from etl_customer ec
                where ec.etl_file_id = %(ETL_FILE_ID)s and not exists (
                   select 'x'
                   from etl_sale
                   where etl_sale.etl_file_id = %(ETL_FILE_ID)s and
                         ec.ship_to_cust_id = etl_sale.ship_to_cust_id
                )
        severity: 1
        format_str: >
                etl_customer_id %s ship_to_cust_id %s name: %s has no sales in load %s
        java_format: >
                etl_customer_id %s ship_to_cust_id %s name: %s has no sales in load %s
    
    -   rule_name: INVALID_CUSTOMER_TOTAL
        table_name: ETL_FILE,
        msg:      Customer Total Record Incorrect
        sql_text: >
                select etl_file_id
                from   etl_customer_tot
                where  etl_file_id = %(ETL_FILE_ID)s
                and customer_count is null or
                    customer_count != (select count(*)
                                     from etl_customer
                                     where etl_file_id = %(ETL_FILE_ID)s)
        severity: 7
        format_str: "Customer total is incorrect for etl_file %s "
        java_format: "Customer total is incorrect for etl_file %s "
    
    
    -   rule_name: SALE_NO_CUST
        table_name:     ETL_SALE
        msg:         Sales record with no customer in load
        sql_text: >
                select etl_sale_id, ship_to_cust_id, etl_file_Id
                from   etl_sale
                where  etl_file_id = %(ETL_FILE_ID)s and
                not exists (select 'x' from etl_customer
                            where etl_customer.ship_to_cust_id = etl_sale.ship_to_cust_id)
        narrative: >
                The customer specified in a sales record does not have
                a corresponding customer record in this load.
    
                A separate test is made to ensure that the customer has
                been previously transmitted by this distributor.
        severity: 3
        format_str: >
             "etl_sale_id %s has ship_to_cust_id %s in load %s sales record but there is no such customer"
        java_format: >
             "etl_sale_id %s has ship_to_cust_id %s in load %s sales record but there is no such customer"
        corrective_action: >
           None required, if the customer was never reported a separate test will mark this load invalid
    
    -   rule_name: UNIDENTIFIED_PRODUCT_IN_SALE
        table_name: ETL_SALE
        msg:     Sales record with no identifiable product in load
        sql_text: >
                select etl_sale_id, mfr_product_id, case_gtin
                from   etl_sale
                where not exists (select product_id
                          from product
                          where product.case_gtin = etl_sale.case_gtin
                                or product.mfr_product_id = etl_sale.mfr_product_id)
                and etl_file_id = %(ETL_FILE_ID)s
                order by etl_sale_id
        narrative: >
                    This sales does not identify an authoritative product
        severity: 6
        format_str: "etl_sale_id: %s, mfr_product_id: %s case_gtin: %s"
        java_format: "etl_sale_id: %s, mfr_product_id: %s case_gtin: %s"
        corrective_action:
                    Update the product master or correct the sales master


Pre-post
--------
The prepost process

Posting
-------

Address Validation
------------------



Processing Steps
----------------

Data is uploaded to the data processing service.

The file loads are analyzed at CDS using an old version of my javautil conditionidentification package.

The data is loading into prepost tables.

The data is posted

A snapshot of the data is made once a day for online web reporting in the form of spreadsheets.

Rebate programs are designed and the sales are tracked and the rebates calculated.

Rebate checks are mailed out.




Distributors
------------
