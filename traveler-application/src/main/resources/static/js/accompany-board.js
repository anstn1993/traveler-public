window.addEventListener("load", () => {
    console.log(location.href);
    const url = new URL(location.href);//현재 페이지 url
    const currentPage = Number(url.searchParams.get("page"));//page parameter value
    const tableBody = document.querySelector("tbody");
    //페이지 이동 버튼
    pageBtns = document.querySelectorAll(".content-section .page-control-box .page-number");
    //url에 parameter로 page를 지정하지 않으면 1페이지 버튼에 효과를 준다.
    if (currentPage === null) {
        pageBtns[0].style = "color: #1FC7EB";
    }
    //url에 설정된 page의 값과 페이지 번호가 같은 곳이 현재 페이지 번호이기 때문에 그곳에 효과를 준다.
    pageBtns.forEach(pageBtn => {
        let value = Number(pageBtn.textContent);
        if (currentPage + 1 === value) {
            pageBtn.style = "color: #1FC7EB";
        }
    });

    //게시판의 body 영역 클릭 리스너
    tableBody.onclick = (event) => {
        console.log(event.target);
        if(event.target.nodeName == "TD") {
            const accompanyIdNode = event.target.parentElement.firstElementChild;
            const accompanyId = accompanyIdNode.textContent;
            location.href = "/accompanies/" + accompanyId;//해당 게시물 페이지로 이동
        }

    }

})