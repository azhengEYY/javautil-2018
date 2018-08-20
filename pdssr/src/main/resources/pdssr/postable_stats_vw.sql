create view etl_sale_post_stats
as
	select etl_file_id,
                count(*)              record_count,
        	count(org_distrib_id) org_distrib_id_count,
        	count(s.org_mfr_id ) org_mfr_id_count,
        	count(s.product_id)  product_id_count,
        	count(s.distributor_customer_id) customer_id_count,
		count(
			case when s.org_distrib_id is not null and
                                  s.org_mfr_id     is not null and
                                  s.product_id     is not null and
				  s.distributor_customer_id is not null
			then 1 else null end
		     ) 
			postable_count
                from etl_sale s
		group by etl_file_id;


 

