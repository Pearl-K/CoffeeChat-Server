# 1) Build stage
FROM gradle:8-jdk17 AS builder
WORKDIR /workspace
COPY . .
RUN gradle clean bootJar -x test --no-daemon


# 2) Run stage
FROM eclipse-temurin:17-jdk
LABEL authors="rkdwl"
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
RUN mkdir -p /logs /dumps
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS \
  -Xlog:gc*:file=/logs/gc.log:time,uptime,level,tags \
  -Xlog:safepoint:file=/logs/gc.log:time,uptime,level \
  -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/dumps/heap.hprof \
  -XX:+UseStringDeduplication \
  -Dcom.sun.management.jmxremote \
  -Dcom.sun.management.jmxremote.port=9999 \
  -Dcom.sun.management.jmxremote.authenticate=false \
  -Dcom.sun.management.jmxremote.ssl=false \
  -jar /app.jar"]