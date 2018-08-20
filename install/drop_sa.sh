set -e 
set -x 
. ./setenv.sh
env
sqlplus  $SYS_UID @ drop_sa
