# .devcontainer/Dockerfile

FROM mcr.microsoft.com/devcontainers/universal:2-linux

RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends \
    openjdk-17-jdk \
    git \
    curl \
    wget \
    unzip \
    && rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

RUN curl -L https://bitbucket.org/iBotPeaches/apktool/downloads/apktool-2.6.3.jar -o /usr/local/bin/apktool.jar \
    && echo '#!/bin/sh' > /usr/local/bin/apktool \
    && echo 'java -jar /usr/local/bin/apktool.jar "$@"' >> /usr/local/bin/apktool \
    && chmod +x /usr/local/bin/apktool

RUN git config --global --add safe.directory /workspace

ENV LANG=en_US.UTF-8 \
    LC_ALL=en_US.UTF-8
