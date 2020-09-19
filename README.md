Traveler project
================

본 프로젝트는 여행자들을 위한 커뮤니티 웹 서비스 입니다. 프로젝트는 크게 oauth2 server, rest api, application 3가지 모듈로 구성하였습니다. 
각 모듈은 다음과 같은 역할을 합니다. 

1. oauth2 server: rest api에 접근하고 제어하기 위한 인증 토큰 발급
2. rest api: 서비스에 사용되는 모든 리소스를 제어할 수 있는 api
3. application: 실제 웹 서비스 모듈로 rest api와 연동되어 동작하는 클라이언트

# 프로젝트 구조

# 데이터베이스 스키마
! [database schema](https://github.com/anstn1993/traveler-public/blob/master/traveler.png?raw=true)
