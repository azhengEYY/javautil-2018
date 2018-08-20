SYS_UID=system/diamondmine@sales_reporting_pdb
USER_UID=sa/tutorial@sales_reporting_pdb

set -x 
set -e
if [ -z $SYS_UID ] ; then 
    	echo SYS_UID not set
    	exit 1
fi
sqlplus $SYS_UID @ setup_privs
# As user
if [ -z $USER_UID ] ; then 
   echo USER_UID not set
   exit 1
fi
#sqlplus $USER_UID @  drop_all
#sqlplus $USER_UID @ create_tables
sqlplus $USER_UID @ logger.pks
sqlplus $USER_UID @ logger.pkb
#sqlplus $USER_UID @ test_dblogging_pkg.sql
#sqlplus $USER_UID @ sample_job_01
