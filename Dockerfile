FROM gradle:jdk11

RUN mkdir -p /opt/sources
RUN mkdir -p /opt/lib
ADD build.gradle.kts /opt/sources
WORKDIR /opt/sources
RUN gradle dependencies --info -x test
ADD . /opt/sources

RUN gradle build --info -x test
RUN mv build/libs/ml-advisor-*-SNAPSHOT.jar /opt/lib/ml-advisor-latest.jar


FROM openjdk:11.0-slim

RUN rm /etc/localtime
RUN ln -s /usr/share/zoneinfo/Asia/Tokyo /etc/localtime

RUN mkdir /opt/lib/
WORKDIR /opt/lib/
COPY --from=0 /opt/lib/ml-advisor-latest.jar ./app.jar

CMD java -Duser.timezone=Asia/Tokyo -jar /opt/lib/app.jar --spring.profiles.active=docker