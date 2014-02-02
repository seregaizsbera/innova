#! /bin/sh

cd "$(dirname "$0")"
exec java -classpath ${project.build.finalName}.jar ru.innova.task.proxy.ProxyServer "$@"
