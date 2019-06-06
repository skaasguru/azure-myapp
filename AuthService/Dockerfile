FROM maven:3.6 AS build

WORKDIR /app

COPY ./ .

RUN mvn package

FROM tomcat:8.5

RUN rm -rf /usr/local/tomcat/webapps/ROOT

COPY --from=build /app/target/authservice.war /usr/local/tomcat/webapps/ROOT.war
