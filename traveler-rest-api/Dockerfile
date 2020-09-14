# 자바 11에서 구동
FROM openjdk:11

#컨테이너 내에서 프로잭트가 점유할 포트 번호
EXPOSE 8080

#도커 컨테이너가 어떤 jar파일을 실행시킬지를 설정
ARG JAR_FILE=target/traveler-rest-api-0.0.1-SNAPSHOT.jar

#위에서 설정한 jar파일의 이름 설정
ADD ${JAR_FILE} traveler-rest-api.jar

#도커 컨테이너가 실행되면 애플리케이션을 실행시킬 진입점 명령어를 설정
ENTRYPOINT ["java", "-jar", "/traveler-rest-api.jar"]

