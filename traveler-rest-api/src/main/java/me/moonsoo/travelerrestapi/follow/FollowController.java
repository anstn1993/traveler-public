package me.moonsoo.travelerrestapi.follow;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.AccountRepository;
import me.moonsoo.commonmodule.account.CurrentAccount;
import me.moonsoo.travelerrestapi.errors.ErrorsModel;
import me.moonsoo.travelerrestapi.properties.AppProperties;
import org.apache.coyote.Response;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/api/accounts")
public class FollowController {

    @Autowired
    FollowService followService;

    @Autowired
    FollowRepository followRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AppProperties appProperties;

    //특정 사용자 follow 핸들러
    @PostMapping("/{accountId}/followings")
    public ResponseEntity followUser(@PathVariable("accountId") Account followingAccount,//팔로우 당하는 사용자
                                     @CurrentAccount Account account,
                                     @RequestBody @Valid FollowDto followDto,
                                     Errors errors) {
        //요청 본문이 유효하지 않은 경우
        if(errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        //자신의 계정이 아닌 다른 사용자 계정으로 follow요청을 하는 경우
        if(!followingAccount.equals(account)) {
            errors.reject("forbidden", "request uri is not valid. account id is not yours");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errors);
        }

        //자기 자신을 팔로우 하려고 하는 경우
        if(followDto.getFollowedAccount().equals(account)) {
            errors.reject("bad request", "you can not follow yourself");
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }
        Optional<Account> followedAccountOtp = accountRepository.findById(followDto.getFollowedAccount().getId());
        //팔로우하려고 하는 사용자가 존재하지 않는 경우
        if(followedAccountOtp.isEmpty()) {
            errors.reject("bad request", "A user you want to follow does not exist. Check a followedAccount.id in the request body");
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }
        followDto.setFollowedAccount(followedAccountOtp.get());
        Follow savedFollow = followService.save(followDto, account);//팔로우 정보 db에 저장

        //Hateoas 적용
        FollowModel followModel = new FollowModel(savedFollow);
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getCreateFollow()).withRel("profile");//profile 링크
        Link getAccountFollowingsLink = linkTo(Follow.class).slash(account.getId()).slash("followings").withRel("get-account-followings");
        Link getAccountFollowersLink = linkTo(Follow.class).slash(account.getId()).slash("followers").withRel("get-account-followers");
        Link deleteAccountFollowingsLink = linkTo(Follow.class).slash(account.getId()).slash("followings").slash(savedFollow.getFollowedAccount()).withRel("delete-account-following");
        followModel.add(profileLink, getAccountFollowingsLink, getAccountFollowersLink, deleteAccountFollowingsLink);
        URI uri = followModel.getLink("self").get().toUri();
        return ResponseEntity.created(uri).body(followModel);
    }

}
