@echo OFF
SET CURRENTDIR="%cd%"
SET JAR_PATH=%~dp0%JIBS-1.0.jar
SET CONFIG_PATH=%~dp0%config/application.properties

IF EXIST %CONFIG_PATH% SET SPRING_LOCALISATION=--spring.config.location=file:///%CONFIG_PATH%

%JAVA_HOME%\bin\java.exe -jar %JAR_PATH% %SPRING_LOCALISATION% %*

SET CURRENTDIR=
SET JAR_PATH=
SET CONFIG_PATH=
SET SPRING_LOCALISATION=