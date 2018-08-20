set -x
pushd javautil-core
mvn clean install
popd
pushd javautil-dblogging
mvn clean install
pushd javautil-condition-identification
mvn clean install
popd
pushd pdssr
mvn clean install

