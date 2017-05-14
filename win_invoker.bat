@echo off

set Bin_java=c:\tools\pleiades\java\8\bin\java
set Option_java=-Xmx2048m -Xms384m
set CLASSPATH=.;lib\*;%CLASSPATH%
set Option_cp=-classpath koracler.jar;lib\*
set Path_work=c:\works\koracler-master

rem cd %Path_work%
rem kotlinc sensitiveSql.kt -include-runtime -d koracler.jar -cp $CLASSPATH

%Bin_java% %Option_java% %Option_cp% SensitiveSqlKt

rem java -Xmx2048m -Xms384m -classpath koracler.jar:lib/* SensitiveSqlKt

	rem in case on Windows with pure console:
		rem c:\tools\pleiades\java\8\bin\java -Xmx2048m -Xms384m -classpath koracler.jar;lib\* SensitiveSqlKt
