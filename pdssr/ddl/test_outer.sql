delete from etl_sale
where case_gtin = '00012345523142'
and ship_to_cust_id = '0000018864';



select etl_sale.ship_to_cust_id,
        count(*),
        sum(extended_net_amt) sum_extended_net_amt
    from
        etl_sale
    left join  product_vw on
        etl_sale.case_gtin = product_vw.case_gtin
    group by etl_sale.ship_to_cust_id,
             etl_sale.case_gtin
    having count(*) = 0;

select * 
from product_vw 
where product_vw.case_gtin = '00012345523142';

select ship_to_cust_id, count(*) 
from etl_sale
where case_gtin = '00012345523142'
group by ship_to_cust_id;

select ship_to_cust_id, count(*) 
from etl_sale
where case_gtin = '00012345523142'
and ship_to_cust_id = '0000018864'
group by ship_to_cust_id;

select * 
from customer_vw
where ship_to_cust_id = '0000018864';

select * 
from product_vw 
where case_gtin = '00012345523142';

select * from etl_sale 
where 
	ship_to_cust_id = '0000018864' and
	case_gtin = '00012345523142';

select etl_sale.ship_to_cust_id,
        count(*),
        sum(extended_net_amt) sum_extended_net_amt
    from
        product_vw
    left outer join  etl_sale on
        product_vw.case_gtin = etl_sale.case_gtin
    where product_vw.case_gtin = '00012345523142'
    group by etl_sale.ship_to_cust_id,
             etl_sale.case_gtin
    order by ship_to_cust_id;

select etl_sale.ship_to_cust_id,
        etl_sale.extended_net_amt,
        product_vw.case_gtin
    from
        product_vw
    left outer join  etl_sale  on
        etl_sale.case_gtin = product_vw.case_gtin
    where product_vw.case_gtin = '00012345523142'
    order by ship_to_cust_id;

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
