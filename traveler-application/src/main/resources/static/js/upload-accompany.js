window.addEventListener("load", function() {
    const uploadBtn = document.querySelector("[value='업로드']");//업로드 버튼
    uploadBtn.addEventListener("click", function() {
        const formData = null;
        const title = document.querySelector("#title-input").value;
        const article = document.querySelector("#article-textarea").value;
        const startDate = document.querySelector("#start-date-input").value;
        const endDate = document.querySelector("#end-date-input").value;
        const location = address;
        const latitude = marker.getPosition().lat();
        const longitude = marker.getPosition().lng();

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

        formData.append("title", title);
        formData.append("article", article);
        formData.append("startDate", startDate);
        formData.append("endDate", endDate);
        formData.append("location", location);
        formData.append("latitude", latitude);
        formData.append("longitude", longitude);
        sendCreateAccompanyRequest(formData);
    });

    function sendCreateAccompanyRequest(formDate) {
        $.ajax().done().fail();
    }
});