#!/bin/bash

echo "Executing Auto Run for Freestyle Search Engine"
echo "\$Header: /share/content/gforge/freestylesearch/freestylesearch/conf/common/searchtest.sh,v 1.1 2007-05-14 15:25:47 hebell Exp $"
echo "\$Name: not supported by cvs2svn $"

DATE=`date +%Y%m%d`
JAVA_HOME=/usr/jdk1.5.0_06
BASE_DIR=/local/content/freestyle/bin

export JAVA_HOME BASE_DIR

ORACLE_HOME=/app/oracle/product/dbhome/9.2.0
PATH=$ORACLE_HOME/bin:$PATH
LD_LIBRARY_PATH=$ORACLE_HOME/lib:$LD_LIBRARY_PATH
TNS_ADMIN=$ORACLE_HOME/network/admin
JAVA_PARMS='-Xms512m -Xmx512m -XX:PermSize=64m'

export JAVA_PARMS ORACLE_HOME TNS_ADMIN PATH LD_LIBRARY_PATH

echo "Executing job as `id`"
echo "Executing on `date`"

$JAVA_HOME/bin/java -client $JAVA_PARMS -classpath $BASE_DIR/cacore32-client.jar:$BASE_DIR/hibernate3.jar:$BASE_DIR/spring.jar:$BASE_DIR/log4j-1.2.13.jar:$BASE_DIR/ojdbc14.jar:$BASE_DIR/freestylesearch.jar gov.nih.nci.cadsr.freestylesearch.test.SearchTest $BASE_DIR/log4j_test.xml $BASE_DIR/SearchTest.xml
