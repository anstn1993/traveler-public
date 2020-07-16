window.addEventListener('load', function () {
    var communitySection = document.querySelector("#community-section");
    var scheduleSection = document.querySelector("#schedule-section");
    var accompanySection = document.querySelector("#accompany-section");

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