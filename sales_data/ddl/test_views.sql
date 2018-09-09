set search_path to test;

create table product
(
    product_cd varchar(14)
);

insert into product (product_cd)
values ('00012345523142');

create table sale
(
	ship_to_cust_id varchar(10),
        product_cd varchar(14),
	extended_net_amt numeric(14,2)
);


select sale.ship_to_cust_id,
        sale.extended_net_amt,
        product.product_cd
    from
        product
    left outer join  sale  on
        sale.product_cd = product.product_cd;
