ARG CACHE_IMAGE

FROM ${CACHE_IMAGE} as jarBuilder
ARG MODULE
WORKDIR  /app
COPY  ./ /app
RUN gradle :${MODULE}:bootJar -i --stacktrace --no-daemon && ls -a

FROM alpine:3.13
ARG MODULE
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.tencent.com/g' /etc/apk/repositories \
    && apk add --update --no-cache openjdk8-jre-base \
    && rm -f /var/cache/apk/*
WORKDIR /app
COPY --from=jarBuilder /app/build/${MODULE}-*-boot.jar  ./app.jar
CMD ["java","-jar","app.jar"]