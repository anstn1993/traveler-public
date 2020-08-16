package me.moonsoo.travelerapplication.follow;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerapplication.account.AccountModel;
import me.moonsoo.travelerapplication.account.SessionAccount;
import me.moonsoo.travelerapplication.deserialize.CustomDeserializer;
import me.moonsoo.travelerapplication.deserialize.CustomPagedModel;
import me.moonsoo.travelerapplication.error.PageNotFoundException;
import me.moonsoo.travelerapplication.properties.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;

@Controller
public class FollowController {

    @Autowired
    private OAuth2RestTemplate oAuth2RestTemplate;

    @Autowired
    private AppProperties appProperties;

    //사용자 팔로잉 처리 핸들러
    @PostMapping("/users/followings")
    public ResponseEntity followUser(@RequestParam("followedAccountId") Account followedAccount,
                                     @SessionAttribute(required = false) SessionAccount account) throws OAuth2AccessDeniedException {
        //로그인 상태가 아닌 경우 403
        if (account == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        headers.add("Accept", MediaTypes.HAL_JSON_VALUE);
        HttpEntity<FollowDto> request = new HttpEntity<>(new FollowDto(followedAccount), headers);
        return oAuth2RestTemplate.postForEntity(appProperties.getRestApiUrl() + "/accounts/" + account.getId() + "/followings", request, FollowDto.class);
    }

    //사용자 언팔로우 처리 핸들러
    @DeleteMapping("/users/followings/{followedAccountId}")
    public ResponseEntity unfollowUser(@PathVariable("followedAccountId") Account followedAccount,
                                       @SessionAttribute(required = false) SessionAccount account) throws OAuth2AccessDeniedException {
        //로그인 상태가 아닌 경우 403
        if (account == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        oAuth2RestTemplate.delete(appProperties.getRestApiUrl() + "/accounts/" + account.getId() + "/followings/" + followedAccount.getId());
        return ResponseEntity.noContent().build();
    }

    //사용자 팔로잉 목록 조회 핸들러
    @GetMapping("/users/{userId}/followings")
    public ResponseEntity getFollowings(@PathVariable("userId") Account targetUser,
                                        @RequestParam String getFollowListUrl) {
        if (targetUser == null) {
            throw new PageNotFoundException();
        }
        ResponseEntity<Object> followingListResponse = oAuth2RestTemplate.getForEntity(getFollowListUrl, Object.class);
        return followingListResponse;
    }

    //사용자 팔로워 목록 조회 핸들러
    @GetMapping("/users/{userId}/followers")
    public ResponseEntity getFollowers(@PathVariable("userId") Account targetUser,
                                        @RequestParam String getFollowListUrl) {
        if (targetUser == null) {
            throw new PageNotFoundException();
        }
        ResponseEntity<Object> followerListResponse = oAuth2RestTemplate.getForEntity(getFollowListUrl, Object.class);
        return followerListResponse;
    }


    private CustomPagedModel<AccountModel> getFollowingList(Account targetUser, String getFollowingListUri) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaTypes.HAL_JSON));
        HttpEntity request = new HttpEntity(headers);
        ResponseEntity<Object> followingListResponse = oAuth2RestTemplate.getForEntity(getFollowingListUri, Object.class);
        CustomDeserializer<AccountModel> customDeserializer = new CustomDeserializer<>();
        CustomPagedModel<AccountModel> followingList = customDeserializer.deseriazizePagedModel(followingListResponse.getBody(), "accountList", AccountModel.class);
        return followingList;
    }

    private CustomPagedModel<AccountModel> getFollowerList(Account targetUser, String getFollowerListUri) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(appProperties.getRestApiUrl() + "/accounts/" + targetUser.getId() + "/followers")
                        .queryParam("size", "10")
                        .queryParam("page", "0")
                        .queryParam("sort", "id,DESC");
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaTypes.HAL_JSON));
        HttpEntity request = new HttpEntity(headers);
        ResponseEntity<Object> followerListResponse = oAuth2RestTemplate.getForEntity(getFollowerListUri, Object.class);
        CustomDeserializer<AccountModel> customDeserializer = new CustomDeserializer<>();
        CustomPagedModel<AccountModel> followerList = customDeserializer.deseriazizePagedModel(followerListResponse.getBody(), "accountList", AccountModel.class);
        return followerList;
    }
}
