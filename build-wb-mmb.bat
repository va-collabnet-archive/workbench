SET TARGETDIR=.
REM SET workbench.version=impl-uktc-1.0-SNAPSHOT


REM SET TARGETLOG=AceWB.txt
REM SET LOGPROPS=AceWB.properties

REM SET LOGPROPDIR=./LogProperties/
REM SET LOGDIR=./LogProperties/



svn up %TARGETDIR%

mvn  -e -f  %TARGETDIR%/pom.xml clean install -Dworkbench.version=%workbench.version%


rem mvn -Djava.util.logging.config.file=%LOGPROPDIR%%LOGPROPS% -e -f  %TARGETDIR%\wb-bdb-pom.xml clean install > %LOGDIR%%TARGETLOG%

rem mvn  -e -o -f  %TARGETDIR%/pom.xml clean 

rem mvn  -e -f  %TARGETDIR%/pom.xml clean install