###
# Build app stage
###

FROM openjdk:8-jdk-alpine as build

RUN apk update && \
    apk add --no-cache git

RUN git clone https://github.com/prianichnikov/CheckUserBot.git

WORKDIR CheckUserBot

RUN ./mvnw package

###
# Prepare image stage
###

FROM alpine:latest

RUN apk update && \
    apk add --no-cache openjdk8

RUN mkdir -p /usr/local/bot

COPY --from=build /CheckUserBot/check-user-bot.jar /usr/local/bot/

ENV BOT_TOKEN=
ENV BOT_NAME=

CMD ["/usr/bin/java", "-jar", "/usr/local/bot/check-user-bot.jar"]