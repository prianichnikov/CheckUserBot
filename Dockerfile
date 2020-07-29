# Build app stage
FROM openjdk:11.0.8-jdk as build
RUN apk update && apk add --no-cache git
RUN git clone https://github.com/prianichnikov/CheckUserBot.git
WORKDIR CheckUserBot
RUN ./mvnw package

# Prepare image stage
FROM openjdk:11.0.8-jre
COPY --from=build /CheckUserBot/check-user-bot.jar /
ENV TOKEN=
ENV JVM_XMS=-Xms64m
ENV JVM_XMX=-Xmx64m
ENV JAVA_OPTS="$JVM_XMS $JVM_XMX"
CMD /usr/bin/java $JAVA_OPTS -jar /check-user-bot.jar