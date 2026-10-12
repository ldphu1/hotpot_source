FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080


ENV TZ=Asia/Ho_Chi_Minh
ENV JAVA_TOOL_OPTIONS="-Duser.timezone=Asia/Ho_Chi_Minh"

CMD ["java","-jar","app.jar"]