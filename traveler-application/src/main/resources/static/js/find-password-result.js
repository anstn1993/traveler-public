window.addEventListener('load', function () {
    //비밀번호 변경 버튼
    const updatePasswordBtn = document.querySelector("input[type='submit']");
    //비밀번호 input
    const passwordInput = document.querySelector("#password-input");
    //비밀번호 확인 input
    const passwordCheckInput = document.querySelector("#password-check-input");
    //사용자가 input 태그에 입력한 데이터들에 대한 피드백을 줄 p 태그
    const messageBox = document.querySelector("#message");

    updatePasswordBtn.onclick = function (event) {
        event.preventDefault();
        const password = passwordInput.value;
        const passwordCheck = passwordCheckInput.value;

        if(password.trim() == "" || passwordCheck.trim() == "") {
            alert("비밀번호와 비밀번호 확인을 모두 입력해주세요");
            return;
        }

        if(password.length < 8 || password.length > 16) {
            messageBox.innerHTML = "비밀번호는 8자리에서 16자리 사이로 입력해주세요.";
            return;
        }

        if(password != passwordCheck) {
            messageBox.innerHTML = "비밀번호와 비밀번호 확인이 일치하지 않습니다.";
            return;
        }

        updatePassword(password);
    }
    window.addEventListener("beforeunload", function (event) {
        event.preventDefault();
        delete event['returnValue'];
        console.log("unload");
        if(authValid) {
            invalidAuthCode();//인증 코드를 세션에서 삭제
        }
    });
});

//인증 코드를 세션에서 지우는 요청을 보내는 함수
function invalidAuthCode() {
    $.ajax(
        {
            url: "/invalidAuthCode",
            type: "POST",
            async: false
        }
    ).done(function (res) {
        console.log("success");
    }).fail(function (res) {
        console.log("fail");
    });
}

//사용자가 입력한 비밀번호로 update를 하는 요청을 보내는 함수
function updatePassword(password, messageBox) {
    $.ajax({
        url: "/find-password/result",
        type: "POST",
        async: false,
        data: {
            "password": password
        }
    }).done(function (res) {
        alert("비밀번호를 변경했습니다. 새로운 비밀번호로 로그인해주세요.");
        location.href = "/login";
    }).fail(function (res) {
        const status = res.status;
        if(status == 500) {
            alert("서버에 문제가 생겼습니다. 잠시 후 다시 시도해주세요.");
        }
        location.href = "/";
    });
}





