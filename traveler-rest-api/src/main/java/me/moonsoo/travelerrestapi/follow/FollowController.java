package me.moonsoo.travelerrestapi.follow;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.AccountRepository;
import me.moonsoo.commonmodule.account.CurrentAccount;
import me.moonsoo.travelerrestapi.errors.ErrorsModel;
import me.moonsoo.travelerrestapi.follow.linkmaker.AbstFollowLinkGenerator;
import me.moonsoo.travelerrestapi.follow.linkmaker.FollowingAccountLinkGenerator;
import me.moonsoo.travelerrestapi.properties.AppProperties;
import org.apache.coyote.Response;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.validation.DirectFieldBindingResult;
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
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        //자신의 계정이 아닌 다른 사용자 계정으로 follow요청을 하는 경우
        if (!followingAccount.equals(account)) {
            errors.reject("forbidden", "Request uri is not valid. Account id is not yours");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errors);
        }

        //자기 자신을 팔로우 하려고 하는 경우
        if (followDto.getFollowedAccount().equals(account)) {
            errors.reject("bad request", "You can not follow yourself");
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        //이미 팔로잉 중인 사용자를 팔로우 하려고 하는 경우
        Optional<Follow> existingFollowOpt = followService.getFollow(followingAccount, followDto.getFollowedAccount());
        if (existingFollowOpt.isPresent()) {
            errors.reject("conflict", "You are already following that user");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorsModel(errors));
        }

        Optional<Account> followedAccountOtp = accountRepository.findById(followDto.getFollowedAccount().getId());
        //팔로우하려고 하는 사용자가 존재하지 않는 경우
        if (followedAccountOtp.isEmpty()) {
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
        Link deleteAccountFollowingsLink = linkTo(Follow.class).slash(account.getId()).slash("followings").slash(savedFollow.getFollowedAccount().getId()).withRel("delete-account-following");
        followModel.add(profileLink, getAccountFollowingsLink, getAccountFollowersLink, deleteAccountFollowingsLink);
        URI uri = followModel.getLink("self").get().toUri();
        return ResponseEntity.created(uri).body(followModel);
    }

    //사용자 팔로잉 목록 조회
    @GetMapping("/{accountId}/followings")
    public ResponseEntity getFollowings(Pageable pageable,
                                        @PathVariable("accountId") Account followingAccount,
                                        PagedResourcesAssembler<Account> assembler,
                                        @CurrentAccount Account account) {
        Page<Account> followedAccounts = followService.findAllFollowedAccounts(followingAccount, pageable);

        //Hateoas 적용
        AbstFollowLinkGenerator linkGenerator = FollowingAccountLinkGenerator.builder()//followedAccounts의 각 요소에 대해서 self, 팔로우/언팔로우 링크를 조건에 맞게 동적으로 제공해주는 객체
                .currentUser(account)
                .resourceAccount(followingAccount)
                .followService(followService)
                .build();
        FollowAccountModelAssembler followAccountModelAssembler = new FollowAccountModelAssembler(linkGenerator, "following");
        PagedModel<FollowAccountModel> accountModels = assembler.toModel(followedAccounts, followAccountModelAssembler);
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getGetAccountFollowingsAnchor()).withRel("profile");//profile 링크
        accountModels.add(profileLink);
        return ResponseEntity.ok(accountModels);
    }

    @GetMapping("/{accountId}/followings/{followedId}")
    public ResponseEntity getFollowing(@PathVariable("accountId") Account followingAccount,
                                       @PathVariable("followedId") Account followedAccount,
                                       @CurrentAccount Account account) {
        //존재하지 않는 리소스 요청인 경우
        if(followingAccount == null || followedAccount == null) {
            return ResponseEntity.notFound().build();
        }

        //사용자가 팔로잉하고 있지 않은 경우
        Optional<Follow> followOpt = followService.getFollow(followingAccount, followedAccount);
        if(followOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        //Hateoas적용
        AbstFollowLinkGenerator linkGenerator = FollowingAccountLinkGenerator.builder()
                .currentUser(account)
                .resourceAccount(followingAccount)
                .followService(followService)
                .build();

        Links links = linkGenerator.makeLinks(followedAccount, "following");//self, create-account-follow or delete-account-follow링크 생성
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getGetAccountFollowingAnchor()).withRel("profile");//profile 링크
        FollowAccountModel followAccountModel = new FollowAccountModel(followedAccount, links);
        followAccountModel.add(profileLink);
        return ResponseEntity.ok(followAccountModel);
    }

    @GetMapping("/{accountId}/followers")
    public ResponseEntity getFollowers(Pageable pageable,
                                       @PathVariable("accountId") Account followedAccount,
                                       PagedResourcesAssembler<Account> assembler,
                                       @CurrentAccount Account account) {

        Page<Account> followingAccounts = followService.findAllFollowingAccounts(followedAccount, pageable);

        //Hateoas적용
        AbstFollowLinkGenerator followLinkGenerator = FollowingAccountLinkGenerator.builder()
                .followService(followService)
                .currentUser(account)
                .resourceAccount(followedAccount)
                .build();

        FollowAccountModelAssembler followAccountModelAssembler = new FollowAccountModelAssembler(followLinkGenerator, "follower");

        PagedModel<FollowAccountModel> accountModels = assembler.toModel(followingAccounts, followAccountModelAssembler);
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getGetAccountFollowersAnchor()).withRel("profile");//profile 링크
        accountModels.add(profileLink);
        return ResponseEntity.ok(accountModels);

    }

    @GetMapping("/{accountId}/followers/{followingId}")
    public ResponseEntity getFollower(@PathVariable("accountId") Account followedAccount,
                                      @PathVariable("followingId") Account followingAccount,
                                      @CurrentAccount Account account) {
        //존재하지 않는 리소스 요청인 경우
        if(followedAccount == null || followingAccount == null) {
            return ResponseEntity.notFound().build();
        }

        //사용자가 팔로잉하고 있지 않은 경우
        Optional<Follow> followOpt = followService.getFollow(followingAccount, followedAccount);
        if(followOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        //Hateoas적용
        AbstFollowLinkGenerator linkGenerator = FollowingAccountLinkGenerator.builder()
                .currentUser(account)
                .resourceAccount(followedAccount)
                .followService(followService)
                .build();

        Links links = linkGenerator.makeLinks(followingAccount, "follower");//self, create-account-follow or delete-account-follow링크 생성
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getGetAccountFollowerAnchor()).withRel("profile");//profile 링크
        FollowAccountModel followAccountModel = new FollowAccountModel(followingAccount, links);
        followAccountModel.add(profileLink);
        return ResponseEntity.ok(followAccountModel);
    }

    @DeleteMapping("/{accountId}/followings/{followedId}")
    public ResponseEntity unfollowUser(@PathVariable("accountId") Account followingAccount,
                                       @PathVariable("followedId") Account followedAccount,
                                       @CurrentAccount Account account) {

        //accountId가 존재하지 않거나 자기 자신이 아닌 경우
        if(followingAccount == null || !followingAccount.equals(account)) {
            Errors errors = new DirectFieldBindingResult(followingAccount,"followingAccount");
            errors.reject("forbidden", "You can not control other user's follow process.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorsModel(errors));
        }

        //자기 자신을 언팔로우 하려고 하는 경우
        if(followingAccount.equals(followedAccount)) {
            Errors errors = new DirectFieldBindingResult(followingAccount,"followingAccount");
            errors.reject("forbidden", "You can not unfollow yourself.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorsModel(errors));
        }

        //존재하지 않는 사용자를 언팔로우하는 경우
        if(followedAccount == null) {
            return ResponseEntity.notFound().build();
        }

        //팔로잉하고 있지 않은 사용자를 언팔로우 하려고 하는 경우
        Optional<Follow> followOpt = followService.getFollow(followingAccount, followedAccount);
        if(followOpt.isEmpty()) {
            Errors errors = new DirectFieldBindingResult(followingAccount, "followingAccount");
            errors.reject("conflict", "You are not following that user");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorsModel(errors));
        }

        Follow follow = followOpt.get();
        followService.delete(follow);//팔로우 데이터 삭제
        return ResponseEntity.noContent().build();
    }
}
