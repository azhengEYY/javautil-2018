SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"
echo $SCRIPTPATH
sqlplus / as sysdba  @ drop_sa_user
sqlplus / as sysdba  @ create_sa_user
