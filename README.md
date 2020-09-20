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

# 기술 스택
언어: java, javascript, html(thymeleaf), css

IDE: IntelliJ

서버 호스팅: aws ec2(ubuntu)

웹 서버: tomcat

프록시 서버: nginx

스토리지: aws s3

데이터베이스: MySQL

프레임워크: spring boot, spring jpa, spring security, spring hateoas, spring rest docs

라이브러리: [query dsl](http://www.querydsl.com/), lombok, spring mail, [green mail](https://greenmail-mail-test.github.io/greenmail/), 
[s3 mock](https://github.com/findify/s3mock) ...

# 세부 모듈 소개

## traveler oauth2 server

rest api에 접근하기 위한 클라이언트 oauth2 인증 서버입니다. oauth2 인증을 통해 토큰 발급을 발급받을 수 있습니다. 토큰은 아래와 같은 요청을 통해서 받을 수 있습니다. 

```
POST /oauth/token? HTTP/1.1
Host: 13.209.207.163
Authorization: Basic dHJhdmVsZXI6cGFzcw==Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW

----WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="username"

testuser
----WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="password"

11111111
----WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="grant_type"

password
----WebKitFormBoundary7MA4YWxkTrZu0gW
```

응답 본문은 아래와 같이 오게 됩니다.

```
{
    "access_token": "ef53b262-e9fb-442a-891e-646b3f6429a3",
    "token_type": "bearer",
    "refresh_token": "ad244fb6-3269-44d5-8147-3b12c38c9ac6",
    "expires_in": 7199,
    "scope": "read write"
}
```

access_token 필드의 값을 rest api 요청시 Authorization 헤더에 담아서 보낼 수 있습니다. rest api 요청 메소드에 따라 access token의 필요 여부가 달라집니다.

실제 요청을 테스트해보고 싶으시다면 username은 'testuser'로, password는 '11111111'로 설정하셔서 요청하시면 됩니다.


## traveler rest api

여행자 커뮤니티 rest api 입니다. 모든 응답 본문에 현재 리소스에서 전이할 수 있는 상태 링크를 제공하여 hateoas를 만족하도록 구성하였습니다. 더불어 profile 링크를 제공해 api 문서에 접근할 수 있도록 하여 self-descriptive를 만족하도록 구성했습니다. 

api에 대한 자세한 명세는 하단의 링크를 참조해주세요. 

[rest api 문서](http://13.209.207.163/docs/index.html)
