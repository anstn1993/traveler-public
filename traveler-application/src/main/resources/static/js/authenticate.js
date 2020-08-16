let authInvalid = true;
window.addEventListener('load', function () {

    const authBtn = document.querySelector("input[type='submit']");
    const timerBox = document.querySelector("#timer-box");
    const messageBox = document.querySelector("#message");
    let time = 600;//총 제한 시간
    let min = "";//분
    let sec = "";//초



    //타이머 실행
    let timer = setInterval(function () {
        min = parseInt(time / 60);
        sec = time % 60;
        timerBox.innerHTML = min + "분 " + sec + "초";
        time--;
        //쿠키에 남은 시간을 저장한다.
        //시간 초과시
        if (time < 0) {
            clearInterval(timer);
            alert("인증 가능한 시간이 초과되었습니다. 다시 인증을 시도해주세요.");
            invalidAuthCode();//인증 코드를 세션에서 삭제해준다.
            location.href = "/";
        }
    }, 1000);

    authBtn.onclick = function (event) {
        event.preventDefault();
        var authCode = document.querySelector("input[name='authCode']").value;//사용자가 입력한 인증코드
        if (authCode == "") {
            alert("인증 코드를 입력하세요.");
            return;
        }
        authenticate(authCode, messageBox);//인증코드 인증 시도
    }
    window.addEventListener("beforeunload", function (event) {
        event.preventDefault();
        delete event['returnValue'];
        console.log("unload");
        if(authInvalid) {
            invalidAuthCode();//인증 코드를 세션에서 삭제
        }
    });
});

//인증 코드를 세션에서 지우는 요청을 보내는 함수
function invalidAuthCode() {
    $.ajax(
        {
            url: "/invalidAuthCode",
            type: "POST"
        }
    ).done(function (res) {
        console.log("success");
    }).fail(function (res) {
        console.log("fail");
    });
}

//사용자가 입력한 인증 코드의 유효성 검사를 하는 요청을 보내는 함수
function authenticate(authCode, messageBox) {
    $.ajax({
        url: "/authenticate",
        type: "POST",
        async: false,
        data: {
            "authCode": authCode
        }
    }).done(function (res) {
        console.log(res);
        console.log(res.authType);
        const authType = res.authType;
        if(authType == "username") {
            authInvalid = false;
            location.href="/find-username/result";
        }
        else {//authType == "password"
            authInvalid = false;
            location.href="/find-password/result";
        }
    }).fail(function (res) {
        messageBox.innerHTML = "인증번호가 잘못되었습니다.";
    });
}


