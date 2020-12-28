# Building the gradle project
FROM gradle:6.7.0-jdk14-openj9 AS build
WORKDIR /home/gradle
ENV GRADLE_USER_HOME /home/gradle/cache

# Fetch and cache the dependencies
COPY --chown=gradle:gradle build.gradle settings.gradle ./
RUN gradle build --no-daemon

# Build the project source files based on the cached dependencies
COPY --chown=gradle:gradle . .
RUN gradle build --no-daemon

# Using an OpenJDK-based runtime for final image
FROM adoptopenjdk:14-jre-openj9-bionic
RUN install -d -m 0744 -o nobody -g nogroup /usr/local/var/log/rafka /usr/local/var/lib/rafka
VOLUME /tmp /usr/local/var/log/rafka /usr/local/var/lib/rafka
EXPOSE $SERVER_PORT
COPY --from=build --chown=nobody:nogroup /home/gradle/build/libs /usr/local/bin/rafka
USER nobody:nogroup
CMD ["bash", "-c", "java $(eval echo $JAVA_OPTS) -jar /usr/local/bin/rafka/rafka.jar"]