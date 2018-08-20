set -x 
set -e
. setenv.sh
# drop the user sa
pushd ddl
sqlplus / as sysdba  @ drop_sa_user
sqlplus / as sysdba  @ create_sa_user 
popd

pushd ../javautil-dblogging/ddl
./quick_install.sh
popd

# 
pushd ../javautil-core
mvn clean install
popd

#
pushd ../javautil-dblogging
mvn clean install



