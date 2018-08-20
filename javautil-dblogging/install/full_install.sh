set -x 
set -e
. setenv.sh
# drop the user sa
sqlplus / as sysdba  @ create_logging_user 



