@echo off
cd /d %~dp0
java -classpath ${project.build.finalName}.jar ru.innova.task.echo.EchoServer %*