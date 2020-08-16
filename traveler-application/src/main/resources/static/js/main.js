window.addEventListener('load', function () {
    const communitySection = document.querySelector(".section--community");
    const scheduleSection = document.querySelector(".section--schedule");
    const accompanySection = document.querySelector(".section--accompany");

    communitySection.onclick = function (e) {
        console.log("communitySection clicked");
        console.log(e);
        location.href = "/posts";//커뮤니티 게시판으로 이동
    }

    scheduleSection.onclick = function (e) {
        console.log("scheduleSection clicked");
        console.log(e);
        location.href="/schedules";//본인의 일정 게시판으로 이동
    }

    accompanySection.onclick = function (e) {
        console.log("accompanySection clicked");
        console.log(e);
        location.href="/accompanies";//동행 구하기 게시판으로 이동
    }
});