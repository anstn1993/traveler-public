let followListResource = null;//팔로우 리스트 요청에 대한 응답의 리소스
window.addEventListener("load", function () {
    const followBtn = document.querySelector(".btn-group li .follow");
    const unfollowBtn = document.querySelector(".btn-group li .unfollow");
    const followingCountBtn = document.querySelector("#following-count");
    const followerCountBtn = document.querySelector("#follower-count");
    const body = document.querySelector(".body__container");
    const userId = location.href.split("/")[4];//사용자 페이지 주인 id
    const getFollowingListUri = "http://localhost:8080/api/accounts/" + userId + "/followings?size=10&page=0&sort=id,DESC";
    const getFollowerListUri = "http://localhost:8080/api/accounts/" + userId + "/followers?size=10&page=0&sort=id,DESC";
    const moreItem = document.createElement("div");//팔로우 모달의 더 보기 아이템
    moreItem.classList.add("more", "off");
    moreItem.textContent = "더 보기";

    const followItemTemplate = document.querySelector("#follow-list-item-template");//사용자 item 템플릿
    let itemBox;//사용자 item이 들어갈 box
    if (followBtn != null && unfollowBtn != null) {
        followBtn.onclick = function (event) {
            event.preventDefault();
            sendFollowRequest(userId, followBtn, unfollowBtn);
            //팔로워 숫자 반영
            let followerCount = parseInt(followerCountBtn.textContent.substr(3, followerCountBtn.textContent.length - 1));
            console.log(followerCount);
            followerCount += 1;
            followerCountBtn.textContent = "팔로워 " + followerCount;
        }

        unfollowBtn.onclick = function (event) {
            event.preventDefault();
            sendUnfollowRequest(userId, followBtn, unfollowBtn);
            //팔로워 숫자 반영
            let followerCount = parseInt(followerCountBtn.textContent.substr(3, followerCountBtn.textContent.length - 1));
            console.log(followerCount);
            followerCount -= 1;
            followerCountBtn.textContent = "팔로워 " + followerCount;
        }
    }
    //팔로잉 개수 클릭 핸들러
    followingCountBtn.onclick = function () {
        createFollowModal("팔로잉");
        itemBox = document.querySelector(".follow-list-modal .main");
        moreItem.classList.add("following");
        moreItem.classList.remove("follower");
        itemBox.append(moreItem);//더 보기 아이템 추가(클래스에 off가 존재하기 때문에 화면에 보이지 않는 상태다.)
        followListResource = fetchFollowList("followings", userId, getFollowingListUri);
    }
    //팔로워 개수 클릭 핸들러
    followerCountBtn.onclick = function () {
        createFollowModal("팔로워");
        itemBox = document.querySelector(".follow-list-modal .main");
        moreItem.classList.add("follower");
        moreItem.classList.remove("following");
        itemBox.append(moreItem);//더 보기 아이템 추가(클래스에 off가 존재하기 때문에 화면에 보이지 않는 상태다.)
        followListResource = fetchFollowList("followers", userId, getFollowerListUri);
    }

    //더 보기 버튼 클릭 핸들러
    moreItem.onclick = function (event) {
        if (moreItem.classList.contains("following")) {
            followListResource = fetchFollowList("followings", userId, followListResource._links.next.href);
        } else {
            followListResource = fetchFollowList("followers", userId, followListResource._links.next.href);
        }
        event.stopPropagation();
    }

    //팔로우 요청 전송 함수
    async function sendFollowRequest(userId, followBtn, unfollowBtn) {
        const formData = new FormData();
        formData.append("followedAccountId", userId);
        try {
            const response = await fetch("/users/followings", {
                method: "POST",
                body: formData
            });
            if(response.ok) {
                toggleFollow(followBtn, unfollowBtn);
            } else if(response.status == 401){
                alert("인증 토큰이 만료되었습니다. 다시 로그인해주세요.");
                location.href = "/login";
            } else {
                alert("문제가 생겼습니다. 잠시 후 다시 시도해주세요.");
            }
        } catch (err) {
            alert("문제가 생겼습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    //언팔로우 요청 전송 함수
    async function sendUnfollowRequest(userId, followBtn, unfollowBtn) {
        try {
            const response = await fetch("/users/followings/" + userId, {
                method: "DELETE"
            });
            if(response.ok) {
                toggleFollow(followBtn, unfollowBtn);
            }
            else if(response.status == 401) {
                alert("인증 토큰이 만료되었습니다. 다시 로그인해주세요.");
                location.href = "/login";
            }
            else {
                alert("문제가 생겼습니다. 잠시 후 다시 시도해주세요.");
            }
        } catch (err) {
            alert("문제가 생겼습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    function setFollowModalItem(followItemTemplate, resource, itemBox) {
        const itemList = resource._embedded.accountList;//팔로우 사용자 목록
        const nextLink = resource._links.next;//팔로우 목록의 다음 페이지 요청 링크
        itemList.forEach(item => {
            // setFollowModalItem(followItemTemplate, item, itemBox);//팔로잉 모달 창의 사용자 아이템 데이터 set
            const followItemNode = document.importNode(followItemTemplate.content, true);
            //사용자 id 셋
            const userIdInput = followItemNode.querySelector("[type='hidden']");
            userIdInput.value = item.id;
            //프로필 이미지 셋
            const profileImg = followItemNode.querySelector(".item-container .img--item img");
            if (item.profileImageUri != null) {
                profileImg.src = item.profileImageUri;
            }
            //닉네임 셋
            const nickname = followItemNode.querySelector(".item-container .nickname--item");
            nickname.textContent = item.nickname;
            //팔로우, 언팔로우 버튼
            const followBtn = followItemNode.querySelector(".item-container .btn--item .follow");
            const unfollowBtn = followItemNode.querySelector(".item-container .btn--item .unfollow");
            //팔로잉 버튼 셋
            const links = item._links;//사용자 리소스 링크

            if (links['delete-account-follow'] == null && links['create-account-follow'] == null) {
                toggleFollow(followBtn, unfollowBtn);
            } else if (links['delete-account-follow'] != null) {
                followBtn.classList.add("off");
            } else {
                unfollowBtn.classList.add("off");
            }
            itemBox.append(followItemNode);
        });
        const moreItem = itemBox.querySelector(".more");
        //다음 페이지가 존재하는 경우
        if (nextLink != null) {
            moreItem.classList.remove("off");
            itemBox.lastElementChild.insertAdjacentElement("afterend", moreItem);
        }
        //다음 페이지가 존재하지 않는 경우
        else {
            moreItem.classList.add("off");
        }
    }

    //팔로잉 혹은 팔로워 사용자 리스트를 서버에서 fetch한 후 화면에 출력
    async function fetchFollowList(followingsOrFollowers, userId, getFollowListUri) {
        try {
            const response = await fetch("/users/" + userId + "/" + followingsOrFollowers + "?getFollowListUrl=" + getFollowListUri);
            if(response.ok) {
                const responseBody = await response.json();
                followListResource = responseBody;
                console.log(responseBody);
                if(responseBody._embedded != undefined) {
                    setFollowModalItem(followItemTemplate, followListResource, itemBox);//팔로잉 모달 창의 사용자 아이템 데이터 set
                }
            }
            else {
                alert("문제가 생겼습니다. 잠시 후 다시 시도해주세요.");
            }
        } catch (err) {
            alert("네트워크 에러:" + err);
        }
    }

    //팔로우 모달 창 생성
    function createFollowModal(modalTitle) {
        const followListModalTemplate = document.querySelector("#follow-list-modal-template");//팔로우 리스트 모달 템플릿
        const followListModal = document.importNode(followListModalTemplate.content, true);//팔로우 리스트 모달
        body.append(followListModal);
        const title = document.querySelector(".follow-list-modal .content .header .title");//모달창 제목 노드
        title.textContent = modalTitle;
        //모달 창 닫기 클릭 리스너
        document.querySelector(".close").onclick = function (event) {
            event.target.parentElement.parentElement.parentElement.remove();
        };
        //메인 영역 클릭 리스너(아이템을 클릭했을 때의 로직을 여기서 처리한다. )
        document.querySelector(".main").onclick = function (event) {
            const target = event.target;
            console.log(target);
            if (!target.classList.contains("follow") &&
                !target.classList.contains("unfollow") &&
                !target.classList.contains("main")) {
                const userId = target.parentElement.querySelector("[type='hidden']").value;
                location.href = "/users/" + userId;
            } else if (target.classList.contains("follow")) {//팔로우 버튼을 클릭한 경우
                const targetUserId = target.parentElement.parentElement.querySelector("[type='hidden']").value;
                const followBtn = target;
                const unfollowBtn = target.previousElementSibling;
                sendFollowRequest(targetUserId, followBtn, unfollowBtn);//팔로우 요청 및 ui 처리
                const editProfileBtn = document.querySelector(".user-info-section .edit-profile");//프로필 수정 버튼
                if (editProfileBtn != null) {//자신의 페이지인 경우
                    //팔로잉 개수 처리
                    let followingCountNode = document.querySelector("#following-count");
                    let followingCount = parseInt(followingCountNode.textContent.substr(4, followingCountNode.textContent.length - 1));
                    followingCount += 1;
                    followingCountNode.textContent = "팔로잉 " + followingCount;
                    console.log(this);
                }
            } else if (target.classList.contains("unfollow")) {//언팔로우 버튼을 클릭한 경우
                const targetUserId = target.parentElement.parentElement.querySelector("[type='hidden']").value;
                const followBtn = target.nextElementSibling;
                const unfollowBtn = target;
                sendUnfollowRequest(targetUserId, followBtn, unfollowBtn);//언팔로우 요청 및 ui 처리
                const editProfileBtn = document.querySelector(".user-info-section .edit-profile");//프로필 수정 버튼
                if (editProfileBtn != null) {//자신의 페이지인 경우
                    //팔로잉 개수 처리
                    let followingCountNode = document.querySelector("#following-count");
                    let followingCount = parseInt(followingCountNode.textContent.substr(3, followingCountNode.textContent.length - 1));
                    followingCount -= 1;
                    followingCountNode.textContent = "팔로잉 " + followingCount;
                    console.log(this);
                    //현재 모달 창이 팔로잉 창인 경우
                    if (this.parentElement.querySelector(".header .title").textContent == "팔로잉") {
                        target.parentElement.parentElement.remove();
                    }
                }
            }
        }
    }

//팔로우, 언팔로우 버튼 교체
    function toggleFollow(followBtn, unfollowBtn) {
        followBtn.classList.toggle("off");
        unfollowBtn.classList.toggle("off");
    }
});

