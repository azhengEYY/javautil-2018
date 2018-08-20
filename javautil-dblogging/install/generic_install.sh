. ./setenv.sh

if [ -z $USER_UID ] ; then
   echo USER_UID is not set >&2
   exit
fi
set -x 
mkdir /scratch/dblogging
set -e
sqlplus $USER_UID @ prepare_connection
sqlplus $USER_UID @ create_tables
sqlplus $USER_UID @ logger.pks
sqlplus $USER_UID @ logger.pkb
sqlplus $USER_UID @ test_dblogging_pkg
sqlplus $USER_UID @ sample_job_01
