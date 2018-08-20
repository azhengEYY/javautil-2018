Record Layouts
==============
This details the reporting format as last posted by custdata.com
on their website.  This is available at 
::

    http://web.archive.org/web/*/custdata.com

Inventory
---------

============================== ======== ====== ====== ========
field_name                     type     offset length format
============================== ======== ====== ====== ========
DISTRIBUTOR_ID                 DIGITS        0     10 {:0>10s}
MFR_ID                         DIGITS       10     10 {:0>10s}
MFR_PRODUCT_ID                 DIGITS       20      8 {:0>8s}
COMMENTS                       TEXT         28     96 {:<96s}
CASES                          INTEGER     124      6 {0:06d}
BOXES                          INTEGER     130      6 {0:06d}
UNITS                          INTEGER     136      6 {0:06d}
CASE_GTIN                      DIGITS      142     14 {:0<14s}
FILLER                         LITERAL     156     12 {:<12s}
RECORD_TYPE                    LITERAL     168      2 {:<2s}
============================== ======== ====== ====== ========

Customer
--------

============================== ======== ====== ====== ========
field_name                     type     offset length format
============================== ======== ====== ====== ========
FILLER_00_05                   LITERAL       0      6 {:>6}
CLASS_OF_TRADE                 TEXT          6      4 {:<4s}
SHIP_TO_CUST_ID                TEXT         10     10 {:0>10s}
CUST_NM                        TEXT         20     30 {:<30s}
ADDR_1                         TEXT         50     30 {:<30s}
ADDR_2                         TEXT         80     30 {:<30s}
CITY                           TEXT        110     25 {:<25s}
STATE                          TEXT        135      2 {:<2s}
POSTAL_CD                      DIGITS      137      9 {:0>9s}
TEL_NBR                        DIGITS      146     10 {:0>10s}
NATIONAL_ACCT_ID               TEXT        156     10 {:0>10s}
SPECIAL_FLG                    TEXT        166      1 {:>1s}
FILLER_1                       LITERAL     167      1 {:>1s}
RECORD_TYPE                    LITERAL     168      2 {:>2s}
============================== ======== ====== ====== ========

Inventory Total
---------------

============================== ======== ====== ====== ========
field_name                     type     offset length format
============================== ======== ====== ====== ========
HEADER                         LITERAL       0     10 {:>1s}
FILLER36                       LITERAL      10     36 {:>36}
INVENTORY_DT                   DATE         46      8 %Y%m%d
FILE_CREATION_DT               DATE         54      8 %Y%m%d
RECORD_CNT_REPORTED            INTEGER      62      9 {0:09d}
FILLER97                       LITERAL      71     97 {:>97}
RECORD_TYPE                    LITERAL     168      2 {:>2}
============================== ======== ====== ====== ========

Customer Total
--------------

============================== ======== ====== ====== ========
field_name                     type     offset length format
============================== ======== ====== ====== ========
HEADER                         LITERAL       0     10 {:>10s}
FILLER_127                     LITERAL      10    127 {:>127s}
CUSTOMER_COUNT                 INTEGER     137      9 {0:09d}
FILLER_22                      LITERAL     146     22 {:>22s}
RECORD_TYPE                    LITERAL     168      2 {:>2s}
============================== ======== ====== ====== ========

Sales
-----

============================== ======== ====== ====== ========
field_name                     type     offset length format
============================== ======== ====== ====== ========
DISTRIB_ID                     TEXT          0     10 {:0>10s}
MFR_ID                         TEXT         10     10 {:0>10s}
MFR_PRODUCT_ID                 TEXT         20      8 {:0>8s}
SHIP_TO_CUST_ID                TEXT         28     10 {:0>10s}
INVOICE_CD                     TEXT         38     10 {:0>10s}
INVOICE_DT                     DATE         48      8 %Y%m%d
SHIP_DT                        DATE         56      8 %Y%m%d
FILLER                         LITERAL      64      9 {:<9s}
EXTENDED_NET_AMT               INTEGER      73      9 {0:09d}
DISTRIB_PRODUCT_REF            TEXT         82     12 {:<12s}
PRODUCT_DESCR                  TEXT         94     30 {:<30s}
CASES_SHIPPED                  INTEGER     124      6 {0:06d}
BOXES_SHIPPED                  INTEGER     130      6 {0:06d}
UNITS_SHIPPED                  INTEGER     136      6 {0:06d}
CASE_GTIN                      TEXT        142     14 {:0>14s}
FILLER_12                      LITERAL     156     12 {:>12s}
RECORD_TYPE                    LITERAL     168      2 {:<2s}
============================== ======== ====== ====== ========

Sales Total
-----------

============================== ======== ====== ====== ========
field_name                     type     offset length format
============================== ======== ====== ====== ========
HEADER                         LITERAL       0     10 {:>10s}
FILLER_28                      LITERAL      10     28 {:>28s}
SALES_START_DT                 DATE         38      8 %Y%m%d
SALES_END_DT                   DATE         46      8 %Y%m%d
FILE_CREATE_DT                 DATE         54      8 %Y%m%d
SALES_REC_CNT                  INTEGER      62      9 {:0>9d}
SUM_EXT_NET_AMT                INTEGER      71     11 {:0>11d}
FILLER_86                      LITERAL      82     86 {:>86s}
RECORD_TYPE                    LITERAL     168      2 {:<2s}
============================== ======== ====== ====== ========
