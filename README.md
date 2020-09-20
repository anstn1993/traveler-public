Traveler project
================

본 프로젝트는 여행자들을 위한 커뮤니티 웹 서비스 입니다. 프로젝트는 크게 oauth2 server, rest api, application 3가지 모듈로 구성하였습니다. 
각 모듈은 다음과 같은 역할을 합니다. 

1. oauth2 server: rest api에 접근하고 제어하기 위한 인증 토큰 발급
2. rest api: 서비스에 사용되는 모든 리소스를 제어할 수 있는 api
3. application: 실제 웹 서비스 모듈로 rest api와 연동되어 동작하는 클라이언트(미완성)

# 프로젝트 구조
![project structure](https://github.com/anstn1993/traveler-public/blob/master/project-structure.PNG?raw=true)

모든 요청은 리버스 프록시 서버로 들어온 후에 톰캣 서버로 전달됩니다. 

application서버는 rest api의 클라이언트로서 db에 직접 접근하지 않고 rest api를 이용해서 모든 리소스를 제어합니다.

# 데이터베이스 스키마
![database schema](https://github.com/anstn1993/traveler-public/blob/master/traveler.png?raw=true)

Table | Description
----- | -----------
account | 사용자 정보 테이블
accompany | 동행 게시물 테이블
accompany_comment | 동행 게시물 댓글 테이블
accompany_child_comment | 동행 게시물 댓글의 대댓글 테이블
schedule | 일정 게시물 테이블
schedule_location | 일정 게시물에 등록되는 위치 테이블
schedule_detail | 일정 게시물에 등록되는 위치에서의 상세 일정 테이블
post | 여행 게시물 테이블
post_image | 여행 게시물에 업로드되는 이미지 테이블
post_like | 여행 게시물의 좋아요 테이블
post_tag | 여행 게시물의 태그 테이블
post_comment | 여행 게시물의 댓글 테이블
post_child_comment | 여행 게시물 댓글의 대댓글 테이블
follow | 사용자 팔로우/팔로잉 테이블


