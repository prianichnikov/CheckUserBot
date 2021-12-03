# Build app stage
FROM openjdk:11-jdk-slim as build
COPY . /bot/
WORKDIR /bot/
RUN chmod +x ./mvnw
RUN ./mvnw package

# Prepare image stage
FROM openjdk:11-jre-slim
COPY --from=build /bot/check-user-bot.jar /
ENV TOKEN=
ENV JVM_XMX=-Xmx128m
ENV JAVA_OPTS="$JVM_XMX"
CMD java $JAVA_OPTS -jar /check-user-bot.jar