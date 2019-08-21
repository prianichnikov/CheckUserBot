# Build app stage
FROM openjdk:8-jdk-alpine as build
RUN apk update && apk add --no-cache git
RUN git clone https://github.com/prianichnikov/CheckUserBot.git
WORKDIR CheckUserBot
RUN ./mvnw package

# Prepare image stage
FROM openjdk:8-jre-alpine
COPY --from=build /CheckUserBot/check-user-bot.jar /
ENV JVM_XMS=-Xms32m
ENV JVM_XMX=-Xmx32m
ENV JAVA_OPTS="$JVM_XMS $JVM_XMX -XshowSettings:vm"
CMD /usr/bin/java $JAVA_OPTS -jar /check-user-bot.jar