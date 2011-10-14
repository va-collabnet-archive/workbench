@echo off
SET LOCAL_ANT_HOME=D:\Ant\apache-ant-1.8.1

echo LOCAL_ANT_HOME = %LOCAL_ANT_HOME%

%LOCAL_ANT_HOME%\bin\ant -buildfile deployCustom-ui.xml