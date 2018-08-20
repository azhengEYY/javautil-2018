    update  vp_distributor_customer c
    set
        class_of_trade ,
        cust_nm        ,
        addr_1         ,
        addr_2         ,
        city           ,
        state          ,
        postal_cd      ,
        cntry_id       ,
        tel_nbr        ,
        national_acct_id ,
        special_flg ,
        effective_dt             
    select 
        class_of_trade ,
        cust_nm        ,
        addr_1         ,
        addr_2         ,
        city           ,
        state          ,
        postal_cd      ,
        cntry_id       ,
        tel_nbr        ,
        national_acct_id ,
        special_flg ,
        effective_dt             
    from etl_customer e
    where
        e.ship_to_cust_id = c.ship_to_cust_id 
