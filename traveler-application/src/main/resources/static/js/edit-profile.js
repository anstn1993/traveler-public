var dataUri;//이미지 데이터 uri(base 64)
window.addEventListener("load", function () {
    // 프로필 수정 폼 리스트
    const profileFormList = document.querySelectorAll(".profile-form");
    // 프로필 수정 메뉴 box
    const editProfileMenuBox = document.querySelector(".edit-profile-section .menu-group ul");
    console.log(editProfileMenuBox.children);
    // 프로필 수정 폼
    const editProfileForm = document.querySelector("#edit-profile-form");
    const profileImg = document.querySelector("#profile");//프로필 이미지 태그
    const profileInput = document.querySelector("#profile-input");//프로필 이미지 선택 input 태그
    const profileInputLabel = document.querySelector("#profile-input-label");//프로필 이미지 선택 input label태그
    const editProfileInputList = document.querySelectorAll("#edit-profile-form .form-input");//프로필 수정 폼 input node list
    const editProfileSubmitBtn = document.querySelector("#edit-profile-form input[type='submit']");//프로필 수정 폼 제출 버튼
    const deleteBtnBox = document.querySelector("#edit-profile-form .delete-btn-box");//프로필 이미지 삭제 버튼 box
    //이미지 삭제 버튼 attribute set
    const deleteBtn = document.createElement("button");
    deleteBtn.className = "btn btn--danger";
    deleteBtn.textContent = "이미지 삭제";

    // 비밀번호 변경 폼
    const changePasswordForm = document.querySelector("#change-password-form");
    const changePasswordInputList = document.querySelectorAll("#change-password-form .form-input");//비밀번호 변경 폼 input list
    const changePasswordSubmitBtn = document.querySelector("#change-password-form [type='submit']");//비밀번호 변경 폼 제출 버튼

    //회원 탈퇴 폼
    const withdrawlForm = document.querySelector("#withdrawl-form");
    const withdrawlCheckBox = document.querySelector("#withdrawl-chb");//회원탈퇴 동의 체크 박스
    const withdrawlSubmitBtn = document.querySelector("#withdrawl-form [type='submit']");//회원탈퇴 폼 제출 버튼

    //프로필 편집, 비밀번호 변경, 회원탈퇴 처리 로딩 다이얼로그
    const loadingBox = document.querySelector(".loading-box");
    //로딩 박스 메세지
    const loadingMessage = document.querySelector(".loading-box p");
    const editProfileLoadingMessage = "프로필 수정 처리 중...";
    const changePasswordLoadingMessage = "비밀번호 변경 처리 중...";
    const withdrawlLoadingMessage = "회원탈퇴 처리 중...";

    //기존 프로필 이미지가 존재하는 경우 이미지 삭제 버튼 set & 이미지 blob을 서버로부터 받아와서 base64로 변환해준다.
    if (profileImg.classList.contains("user-img")) {
        //add delete btn
        deleteBtnBox.innerHTML = "";
        deleteBtnBox.append(deleteBtn);
        // const tempImage = new Image();
        // tempImage.src = profileImg.src;
        // tempImage.crossOrigin = "Anonymous";
        // const canvas = document.createElement("canvas");
        // canvas.width = tempImage.clientWidth;
        // canvas.height = tempImage.clientHeight;
        //
        // const context = canvas.getContext("2d");
        // context.drawImage(profileImg, 0, 0);
        // canvas.toBlob(function (blob) {
        //     //blob to base 64
        //     const reader = new FileReader();
        //     reader.readAsDataURL(blob);
        //     reader.onload = function () {
        //         console.log(reader);
        //         dataUri = reader.result;
        //     }
        // });
        const xhr = new XMLHttpRequest();
        xhr.responseType = "blob";
        xhr.onload = function () {
            if (xhr.status == 200) {
                const imageBlob = xhr.response;
                //blob to base 64
                const reader = new FileReader();
                reader.readAsDataURL(imageBlob);
                reader.onload = function () {
                    console.log(reader);
                    dataUri = reader.result;
                }
            }
        }
        xhr.open("GET", profileImg.src);
        xhr.send();
    }

    // 메뉴 선택에 따른 폼 전환
    editProfileMenuBox.onclick = function (event) {
        const target = event.target;//클릭된 메뉴 노드
        const menuList = editProfileMenuBox.children;
        for (let i = 0; i < menuList.length; i++) {
            if (menuList[i] === target) {
                menuList[i].style.backgroundColor = "#337AB7";
            } else {
                menuList[i].style.backgroundColor = "transparent";
            }
        }

        if (target.className == "edit-profile-menu") {
            editProfileForm.classList.remove("off")
            changePasswordForm.classList.add("off")
            withdrawlForm.classList.add("off")
        } else if (target.className == "change-password-menu") {
            editProfileForm.classList.add("off")
            changePasswordForm.classList.remove("off")
            withdrawlForm.classList.add("off")
        } else {
            editProfileForm.classList.add("off")
            changePasswordForm.classList.add("off")
            withdrawlForm.classList.remove("off")
        }
    }


    //프로필 이미지 선택 시 콜백되는 이벤트
    profileInput.onchange = function (event) {
        console.log(event);
        const file = event.target.files[0];
        if (file == null) return;//이미지 선택 창을 취소할 경우를 대비해서 null 체크
        if (!validImageType(file)) {//이미지 파일인지 검사
            alert("이미지 파일만 선택 가능합니다.");
            return;
        }
        console.log(file);
        if (file != null) {
            loadImage(file, profileImg);
        }
        deleteBtnBox.innerHTML = "";
        deleteBtnBox.append(deleteBtn);
    }

    //드래그앤 드롭 이벤트
    //이미지가 드래그앤 드롭 박스로 들어오는 최초에 한 번 호출
    profileInputLabel.ondragenter = function (event) {
        event.stopPropagation();
        event.preventDefault();
    }
    //드래그앤 드롭 박스에서 이미지를 움직이는 순간 계속 호출
    profileInputLabel.ondragover = function (event) {
        event.stopPropagation();
        event.preventDefault();//기본 동작인 이미지 파일을 새 창으로 로드하는 것을 방지.
    }
    //이미지를 drop하는 순간 호출
    profileInputLabel.ondrop = function (event) {
        event.stopPropagation();
        event.preventDefault();//기본 동작인 이미지 파일을 새 창으로 로드하는 것을 방지.
        console.log(event);
        const file = event.dataTransfer.files[0];
        console.log(file);
        if (!validImageType(file)) {//이미지 파일인지 검사
            alert("이미지 파일만 선택 가능합니다.");
            return;
        }
        console.log(file);
        if (file != null) {
            loadImage(file, profileImg);
        }
        deleteBtnBox.innerHTML = "";
        deleteBtnBox.append(deleteBtn);
    }

    // 이미지 삭제 버튼 클릭시 콜백되는 이벤트
    deleteBtn.onclick = function (event) {
        console.log(event);
        profileInput.value = "";//file input태그에서 value 초기화
        dataUri = null;
        profileImg.src = "/image/profile-setting.png";//이미지 삭제
        deleteBtnBox.innerHTML = "";
    }

    //프로필 수정 폼 제출 콜백 이벤트
    editProfileSubmitBtn.onclick = function (event) {
        event.preventDefault();
        if (isEmptyOrWhitespace(editProfileInputList)) {
            alert("모든 항목을 채워주세요.");
            return;
        }
        const formData = new FormData();
        //프로필 이미지 추가
        if (dataUri != null) {
            formData.append("imageFile", dataURLToBlob(dataUri));
        }
        console.log(editProfileInputList);
        for (let i = 0; i < editProfileInputList.length; i++) {
            const name = editProfileInputList[i].name;
            const value = editProfileInputList[i].value;
            if (name != "sex") {
                formData.append(name, value);
            } else {
                if (editProfileInputList[i].checked) {
                    formData.append(name, value);
                }
            }
        }
        toggleLoadingBox(loadingBox, loadingMessage, editProfileLoadingMessage);
        sendEditProfileRequest(formData);
    }

    //비밀번호 변경 폼 제출 콜백 이벤트
    changePasswordSubmitBtn.onclick = function (event) {
        event.preventDefault();
        if (isEmptyOrWhitespace(changePasswordInputList)) {
            alert('모든 항목을 채워주세요.');
            return;
        }
        const formData = new FormData();
        for (let i = 0; i < changePasswordInputList.length; i++) {
            const name = changePasswordInputList[i].name;
            const value = changePasswordInputList[i].value;
            formData.append(name, value);
        }

        toggleLoadingBox(loadingBox, loadingMessage, changePasswordLoadingMessage);
        sendChangePasswordRequest(formData);
    }

    //회원 탈퇴 폼 제출 콜백 이벤트
    withdrawlSubmitBtn.onclick = function (event) {
        event.preventDefault();
        if (!withdrawlCheckBox.checked) {
            alert("회원탈퇴 동의 체크박스를 체크해주세요.");
            return;
        }
        toggleLoadingBox(loadingBox, loadingMessage, withdrawlLoadingMessage);
        sendWithdrawlRequest(loadingBox);
    }


//선택한 이미지 파일을 썸네일로 만들어서 화면에 출력
    function loadImage(file, profileImg) {
        const reader = new FileReader();//파일 reader
        console.log(reader);
        reader.readAsDataURL(file);//이미지 파일을 읽어들인다. trigger reader onload event

        reader.onload = function () {
            console.log("reader onload");
            const tempImage = new Image();//썸네일 이미지 생성를 담을 image 객체
            tempImage.src = reader.result;//data-uri를 이미지 객체에 주입. trigger image onload
            tempImage.onload = function () {
                //이미지 리사이즈를 위한 캔버스 객체 생성
                const canvas = document.createElement("canvas");
                const canvasContext = canvas.getContext('2d');

                const maxSize = 720;//리사이징할 이미지 파일의 크기

                //실제 이미지 사이즈
                let width = tempImage.width;
                let height = tempImage.height;

                //크기 리사이징
                if (width > height) {
                    if (width > maxSize) {
                        height *= maxSize / width;
                        width = maxSize;
                    }
                } else {
                    if (height > maxSize) {
                        width *= maxSize / height;
                        height = maxSize;
                    }
                }

                //캔버스 크기 설정
                canvas.width = width;
                canvas.height = height;

                //tempImage를 캔버스 위에 그린다.
                canvasContext.drawImage(this, 0, 0, width, height);
                //이미지 객체를 다시 data-uri형태로 바꿔서 img태그에 로드한다.
                dataUri = canvas.toDataURL("image/*");
                console.log(dataUri);
                profileImg.src = dataUri;
            }
        }
    }

//canvas의 data url을 blob 객체로 변환해서 file로 업로드하기 위한 데이터로 변환
    var dataURLToBlob = function (dataURL) {
        console.log(dataURL);
        const BASE64_MARKER = ';base64,';
        //base 64로 인코딩되어있지 않은 경우
        if (dataURL.indexOf(BASE64_MARKER) == -1) {
            const parts = dataURL.split(',');
            const contentType = parts[0].split(':')[1];//mime type(media type)
            const raw = parts[1];//데이터 그 자체
            return new Blob([raw], {type: contentType});
        }
        const parts = dataURL.split(BASE64_MARKER);
        const contentType = parts[0].split(':')[1];
        const raw = window.atob(parts[1]);//window.atob()는 base 64를 디코딩하는 메소드
        const rawLength = raw.length;
        const uInt8Array = new Uint8Array(rawLength);
        for (let i = 0; i < rawLength; ++i) {
            uInt8Array[i] = raw.charCodeAt(i);
        }
        return new Blob([uInt8Array], {type: contentType});
    };

    //서버로 프로필 수정 요청을 보내는 함수
    async function sendEditProfileRequest(formData) {
        try {
            const response = await fetch(location.href, {
                method: "POST",
                body: formData
            });
            toggleLoadingBox(loadingBox, loadingMessage, editProfileLoadingMessage);
            if (response.ok) {
                alert("프로필 수정을 완료했습니다!");
            } else if (response.status == 401) {
                alert("인증 토큰이 만료되었습니다. 다시 로그인해주세요.");
                location.href = "/login";
            } else {
                let message;
                const responseBody = await response.json();
                try {
                    message = responseBody.responseJSON[0].defaultMessage;
                } catch (error) {
                    alert("문제가 생겼습니다. 잠시 후 다시 시도해주세요.");
                    return;
                }
                alert(message);

            }
        } catch (err) {
            alert(err);
        }
    }

    //서버로 비밀번호 변경 요청을 보내는 함수
    async function sendChangePasswordRequest(formData) {
        const userId = location.href.split("/")[4];
        console.log(userId);
        try {
            const response = await fetch("/users/" + userId + "/password", {
                method: "PUT",
                body: formData
            });
            if (response.ok) {
                toggleLoadingBox(loadingBox, loadingMessage, changePasswordLoadingMessage);
                alert("비밀번호 변경을 완료했습니다!");
            } else if (response.status == 401) {
                alert("인증 토큰이 만료되었습니다. 다시 로그인해주세요.");
                location.href = "/login";
            } else {
                let message;
                const responseBody = await response.json();
                try {
                    message = responseBody.responseJSON[0].defaultMessage;
                } catch (err) {
                    alert(err);
                    return;
                }
                alert(message);
            }
        } catch (err) {
            alert(err);
        }

    }

    async function sendWithdrawlRequest(loadingBox) {
        const userId = location.href.split("/")[4];
        console.log(userId);
        try {
            const response = await fetch("/users/" + userId + "/withdrawl", {
                method: "DELETE"
            });
            toggleLoadingBox(loadingBox, loadingMessage, withdrawlLoadingMessage);
            if (response.ok) {
                alert("회원탈퇴가 완료되었습니다!");
                location.href = "/";//메인 페이지로 이동
            } else if (response.status == 401) {
                alert("인증 토큰이 만료되었습니다. 다시 로그인해주세요.");
                location.href = "/login";
            } else {
                let message;
                const responseBody = await response.json();
                try {
                    message = responseBody.responseJSON[0].defaultMessage;
                } catch (err) {
                    alert(err);
                    return;
                }
                alert(message);
            }
        } catch (err) {
            alert(err);
        }
    }


    //파일이 이미지 파일인지 검사하는 함수
    function validImageType(file) {
        const type = file.type;
        if (type.indexOf("image") == -1) {
            return false;
        }
        return true;
    }

    //자기 소개를 제외한 모든 폼 데이터들이 채워졌는지 확인
    //공백이나 빈 공간이 있으면 true, 모두 입력되어 있으면 false
    function isEmptyOrWhitespace(inputList) {
        for (let i = 0; i < inputList.length; i++) {
            if (inputList[i].name == "introduce") {
                continue;
            }
            if (inputList[i].value.trim() == "") {
                return true;
            }
        }
        return false;
    }

    //로딩 박스를 on/off하는 함수
    function toggleLoadingBox(loadingBox, loadingMessage, message) {
        loadingMessage.textContent = message;
        loadingBox.classList.toggle("on");
    }
});
