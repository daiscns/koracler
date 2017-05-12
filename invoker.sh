#!/bin/bash -x
umask 022

cd ~/works/ccti01/koracler
export CLASSPATH=.:lib/ojdbc6.jar:lib/snakeyaml.jar:lib/sqlite-jdbc.jar:$CLASSPATH

if [ $# -lt 1 ]; then
	kotlinc sensitiveSql.kt -include-runtime -d koracler.jar -cp $CLASSPATH
	if [ $? -ne 0 ]; then
		exit 0
	fi
fi

java -Xmx2048m -Xms384m -classpath koracler.jar:lib/* SensitiveSqlKt
	# in case on Windows with pure console:
		# c:\tools\pleiades\java\8\bin\java -Xmx2048m -Xms384m -classpath koracler.jar;lib\* SensitiveSqlKt
		
# java -Xmx1536m -Xms384m -classpath $JAR_PATH amazonTool_Com.AmazonTool \
	# -pc $CONFIG_PATH
	# > /dev/null
