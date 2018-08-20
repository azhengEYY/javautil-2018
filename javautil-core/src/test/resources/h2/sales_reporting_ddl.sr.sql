create table org (
    org_id bigint auto_increment primary key not null,
    org_cd varchar(16),
    org_nm varchar(32)
)
;--
create table org_datafeed (
    org_id bigint auto_increment primary key not null references org(org_id)
)
;--
create table org_mfr (
    org_id bigint auto_increment primary key not null references org(org_id),
    cds_mfr_id varchar(10) not null 
)
;--
create table org_distrib(
    org_id bigint auto_increment primary key not null references org(org_id))
;--
create table org_customer(
   org_id bigint auto_increment primary key not null references org(org_id)
)
;--
create table distributor_customer (
    distributor_customer_id bigint auto_increment not null primary key,
    org_distrib_id     integer not null references org_distrib(org_id),
    ship_to_cust_id    varchar(10)
)
;--
create unique index distributor_customer_nk  on distributor_customer(ship_to_cust_id,org_distrib_id)
;--
create table product (
    product_id      bigint auto_increment primary key,
    mfr_id          integer not null,
    product_descr   varchar(60),
    mfr_product_id  varchar(8),
    case_gtin       char(14),
    box_gtin        char(14),
    unit_gtin       char(14),
    units_per_box   numeric(9),
    units_per_case  numeric(9)
)
;--
create table etl_file (
    etl_file_id         bigint auto_increment primary key not null,
    rpt_org_id          integer not null references org_datafeed(org_id),
    datafeed_org_id     integer not null
)
;--
create table etl_customer (
    etl_customer_id     bigint auto_increment primary key not null,
    ship_to_cust_id     varchar(10) not null,
    etl_file_id         integer  not null references etl_file(etl_file_id),
    line_number         integer not null,
    class_of_trade      varchar(4),
    cust_nm             varchar(30),
    addr_1              varchar(30),
    addr_2              varchar(30),
    city                varchar(25),
    state               varchar(2),
    postal_cd           varchar(9),
    cntry_id            varchar(3),
    tel_nbr             varchar(10),
    national_acct_id    varchar(10),
    special_flg         varchar(1),
    foreign key (etl_file_id) references etl_file(etl_file_id)
)

;--
create table etl_customer_tot (
    etl_file_id        bigint auto_increment primary key references etl_file(etl_file_id),
    line_number         integer not null,
    customer_count     numeric(9) not null
)
;--
create table etl_inventory (
    etl_inventory_id        bigint auto_increment primary key,
    etl_file_id             integer  not null references etl_file(etl_file_id),
    line_number             integer,
    distributor_id          varchar(10),
    mfr_id                  varchar(10),
    mfr_product_id          varchar(8),
    comments                varchar(96),
    cases                   numeric(6),
    boxes                   numeric(6),
    units                   numeric(6),
    case_gtin               varchar(14),
    inventory_qty           numeric(9,3),
    inventory_unit_meas_id  varchar(3)
)
;--
create table etl_inventory_tot (
    etl_file_id         bigint auto_increment primary key references etl_file(etl_file_id),
    line_number         integer,
    inventory_dt        date,
    file_creation_dt    date,
    record_cnt_reported numeric(8),
    record_cnt_actual   numeric(8)
)
;--
create table etl_sale (
    etl_sale_id         bigint auto_increment primary key,
    etl_file_id         integer not null references etl_file(etl_file_id),
    line_number         integer,
    distrib_Id          varchar(10) not null,
    mfr_id              varchar(10) not null,
    mfr_product_id      varchar(8),
    ship_to_cust_id     varchar(10) not null,
    invoice_cd          varchar(10),
    invoice_dt          date,
    ship_dt             date,
    /* should have a currency */
    extended_Net_Amt    numeric(9,2),
    curr_cd             varchar(3),
    distrib_product_ref varchar(12),
    product_descr       varchar(32),
    cases_shipped       numeric(9),
    boxes_shipped       numeric(9),
    units_shipped       numeric(9),
    case_gtin           varchar(14),
    product_id          integer references product(product_id),
    org_distrib_id      integer references org_distrib(org_id),
    distributor_customer_id integer
        references distributor_customer(distributor_customer_id),
    org_mfr_id          integer references org_mfr(org_id)
)
;--
create index etl_sale_etl_file_id on etl_sale(etl_file_id)
;--
create table etl_sale_tot (
         etl_file_id     integer primary key references etl_file,
         line_number     integer,
         sales_start_dt  date,
         sales_end_dt    date,
         file_create_dt  date,
         sales_rec_cnt   integer,
         sum_ext_net_amt numeric(12,2)
)
;--

create table product_pkg (
    product_pkg_id      bigint auto_increment primary key,
    product_id          integer not null,
    pkg_id              integer not null,
    pkg_qty_numerator   numeric(5) not null,
    pkg_qty_divisor     numeric(5) not null
)
;--
create table product_nomen (
    product_nomen_id   bigint auto_increment  primary key,
    org_id              integer not null references org(org_id),
    product_id          integer references product(product_id),
    product_ref_cd      varchar(30),
    case_gtin           varchar(14),
    descr               varchar(60)
)
;--

create table vp_distributor_customer (
    vp_distributor_customer_id  bigint auto_increment primary key,
    org_distributor_id          integer references org_distrib,
    ship_to_cust_id             varchar(10) not null,
    etl_customer_id             integer not null references etl_customer,
    class_of_trade              varchar(4),
    cust_nm                     varchar(30),
    addr_1                      varchar(30),
    addr_2                      varchar(30),
    city                        varchar(25),
    state                       varchar(2),
    postal_cd                   varchar(9),
    cntry_id                    varchar(3),
    tel_nbr                     varchar(10),
    national_acct_id            varchar(10),
    special_flg                 varchar(1),
    effective_dt                date
)

;--
create table customer (
    customer_id  bigint auto_increment primary key references org(org_id)
)
;--
create table product_suspect_hdr (
    product_suspect_hdr_id bigint auto_increment not null primary key,
    descr                  varchar(60)
)
;--
create table product_suspect_dtl (
    product_suspect_dtl_id bigint auto_increment primary key not null,
    product_id             integer not null references product(product_id),
    product_suspect_hdr_id integer not null
)

;--
create table post_sale (
    etl_sale_id     bigint auto_increment primary key references etl_sale(etl_sale_id),
    org_distrib_id  integer references org_distrib(org_id),
    org_mfr_id      integer not null references org_mfr(org_id),
    product_id      integer not null references product(product_id),
    distributor_customer_id integer not null references distributor_customer(distributor_customer_id),
    normalized_qty  numeric (13,5) not null,
    sale_amt        numeric (13,5) not null
)
;--
create index post_sale_idx1 on post_sale(org_distrib_id)
;--
create table validated_address (
    validated_address_id bigint auto_increment primary key,
    addr_1              varchar(30),
    addr_2              varchar(30),
    city                varchar(25),
    state               varchar(2),
    postal_cd           varchar(9),
    valid_addr_1        varchar(30),
    valid_addr_2        varchar(30),
    valid_city          varchar(25),
    valid_state         varchar(2),
    valid_postal_cd     varchar(9),
    validation_msg      varchar(200)
)
;--

create view load_conditions as
select
        cast(p.parm_value_str as bigint) etl_file_id,
        cond.table_name,
        cond.condition_name,
        count(row.ut_condition_run_step_id) condition_count
from    ut_condition_row_msg row,
        ut_condition_run_step step,
        ut_condition_run run,
        ut_condition cond,
        ut_condition_run_parm p
where 	row.ut_condition_run_step_id  = step.ut_condition_run_step_id and
       	step.ut_condition_run_id = run.ut_condition_run_id and
       	step.ut_condition_id = cond.ut_condition_id and
    	p.ut_condition_run_id = run.ut_condition_run_id and
    	p.parm_nm= 'ETL_FILE_ID'
group by cond.table_name,
    	cond.condition_name,
    	p.parm_value_str
order by cast(p.parm_value_str as bigint),
		cond.table_name, 
		cond.condition_name
;--


--create view product_nomen_xref as
--select p.product_id ,
--       pn.org_id org_id_nomen,
--       n.org_cd org_cd_nomen,
--       n.org_nm org_name_nomen,
--       mfr.org_cd org_cd_mfr,ns
--       mfr.org_nm org_nm_mfr,
--       pn.product_ref_cd,
--       pn.descr,
--       p.product_descr,
--       mfr_id
--from   product_nomen pn,
--       product p,
--       org mfr,
--       org n
--where  p.product_id = pn.product_id
--       and mfr.org_id = p.mfr_id
--       and pn.org_id = n.org_id
--
/*
create view etl_sale_stats as
  select etl_file_id,
        count(*) record_count,
        count(case_gtin) case_gtin_count,
        count(product_id) product_id_count,
        count(distinct product_id) distinct_product_id_count,
        count(distinct distrib_product_ref) distinct_product_ref_count,
        count(distinct(ship_to_cust_id)) distinct_cust_id_count,
        count(distinct mfr_product_id) distinct_mfr_product_id_count
from    etl_sale
group by
        etl_file_id

create view etl_file_stat as
select etl_file_id,
    count(*)        record_count,
    min(invoice_dt) min_invoice_dt,
    max(invoice_dt) max_invoice_dt,
    count(distinct(mfr_product_id)) count_distinct_mfr_product_id,
    count(distinct(distrib_prod_ref)) count_distinct_distrib_prod_ref
from etl_sale
group by etl_file_id
*/
;--

