window.addEventListener("load", function() {
    const uploadBtn = document.querySelector("[value='업로드']");//업로드 버튼
    //업로드 처리 로딩 다이얼로그
    const loadingBox = document.querySelector(".loading-box");
    uploadBtn.addEventListener("click", function() {
        const title = document.querySelector("#title-input").value;
        const article = document.querySelector("#article-textarea").value;
        const startDate = document.querySelector("#start-date-input").value;
        const endDate = document.querySelector("#end-date-input").value;
        const location = address;
        const latitude = marker.getPosition().lat();
        const longitude = marker.getPosition().lng();
        console.log("title: " + title);
        console.log("article: " + article);
        console.log("startDate: " + startDate);
        console.log("endDate: " + endDate);
        console.log("location: " + location);
        console.log("latitude: " + latitude);
        console.log("longitude: " + longitude);

        // empty data check
        if(location === undefined) {
            alert("여행 장소를 지정해주세요.")
            return;
        }
        if(title.trim() === "") {
            alert("게시물의 제목을 입력해주세요.")
            return;
        }
        if(article.trim() === "") {
            alert("게시글을 입력해주세요.")
            return;
        }
        if(startDate.trim() === "") {
            alert("여행의 시작 날짜를 입력해주세요.")
            return;
        }
        if(endDate.trim() === "") {
            alert("여행의 종료 날짜를 입력해주세요.")
            return;
        }

        //compare start date and end date
        const startDateObj = new Date(startDate);
        const endDateObj = new Date(endDate);
        if(startDateObj >= endDateObj) {
            alert("여행 종료 날짜가 여행 시작 날짜보다 빠를 수 없습니다.");
            return;
        }

        const requestBody = {
            "title": title,
            "article": article,
            "startDate": startDate,
            "endDate": endDate,
            "location": location,
            "latitude": latitude,
            "longitude": longitude
        };
        sendCreateAccompanyRequest(requestBody);
    });

    async function sendCreateAccompanyRequest(requestBody) {
        toggleLoadingBox(loadingBox);
        const response = await fetch("/accompanies", {
            method: "POST",
            headers: {
              "Content-Type": "application/json;charset=utf-8"
            },
            body: JSON.stringify(requestBody)
        });
        toggleLoadingBox(loadingBox);
        if(response.ok) {
            alert("업로드 성공!");
            location.href = "/accompanies"
        }
        else if(response.status == 401) {
            alert("인증 토큰이 만료되었습니다. 다시 로그인해주세요.");
            location.href = "/login";
        }
        else {
            const responseBody = await response.json();
            let message;
            try {
                message = responseBody.responseJSON[0].defaultMessage;
            } catch (error) {
                alert("문제가 생겼습니다. 잠시 후 다시 시도해주세요.");
                return;
            }
            alert(message);
        }
    }

    //로딩 박스를 on/off하는 함수
    function toggleLoadingBox(loadingBox) {
        loadingBox.classList.toggle("on");
    }
});