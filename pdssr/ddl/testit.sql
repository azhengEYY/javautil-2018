
create view product_test as 
select * 
from product_vw 
where product_vw.case_gtin = '00012345523142';

select * from product_test;

create view etl_sale_test as
select * from etl_sale
where 1 = 2;


select etl_sale_test.ship_to_cust_id,
        etl_sale_test.extended_net_amt,
        product_test.case_gtin
    from
        product_test
    left outer join  etl_sale_test  on
        etl_sale_test.case_gtin = product_test.case_gtin;

create table product_t
(
    case_gtin varchar(14)
);

insert into product_t (case_gtin)
values ('00012345523142');

create table sale_t
(
	ship_to_cust_id varchar(10),
        case_gtin varchar(14),
	extended_net_amt numeric(14,2)
);


select sale_t.ship_to_cust_id,
        sale_t.extended_net_amt,
        product_t.case_gtin
    from
        product_t_
    left outer join  sale_t  on
        sale_t.case_gtin = product_t.case_gtin
    where product_t.case_gtin = '00012345523142'
    order by ship_to_cust_id;
