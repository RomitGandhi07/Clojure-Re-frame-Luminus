FROM openjdk:8-alpine

COPY target/uberjar/booksclubwithauth.jar /booksclubwithauth/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/booksclubwithauth/app.jar"]
