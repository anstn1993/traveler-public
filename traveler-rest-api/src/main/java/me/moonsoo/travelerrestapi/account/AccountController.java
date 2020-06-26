package me.moonsoo.travelerrestapi.account;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.CurrentAccount;
import me.moonsoo.travelerrestapi.email.EmailService;
import me.moonsoo.travelerrestapi.errors.ErrorsModel;
import me.moonsoo.travelerrestapi.follow.Follow;
import me.moonsoo.travelerrestapi.follow.FollowService;
import me.moonsoo.travelerrestapi.properties.AppProperties;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class AccountController {

    @Autowired
    AccountService accountService;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    EmailService emailService;

    @Autowired
    AppProperties appProperties;

    @Autowired
    FollowService followService;

    //사용자 추가 핸들러
    @PostMapping("/api/accounts")
    public ResponseEntity createAccount(@RequestPart List<MultipartFile> imageFile,
                                        @RequestPart("account") @Valid AccountDto accountDto,
                                        Errors errors) {

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        if (imageFile.size() >= 2) {//프로필 이미지가 2개 이상이 넘어오는 경우
            errors.reject("imageFile", "Max image count is 1.");
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        Account account = modelMapper.map(accountDto, Account.class);
        Account savedAccount = null;
        try {
            //hateoas 적용
            savedAccount = accountService.save(account, imageFile);
            AccountModel accountModel = new AccountModel(savedAccount);
            WebMvcLinkBuilder linkBuilder = linkTo(methodOn(AccountController.class).createAccount(imageFile, accountDto, errors));
            Link selfLink = linkBuilder.slash(savedAccount.getId()).withSelfRel();
            Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getCreateAccountAnchor()).withRel("profile");//profile 링크
            Link getAccountsLink = linkBuilder.withRel("get-accounts");
            Link updateLink = linkBuilder.slash(savedAccount.getId()).withRel("update-account");
            Link deleteLink = linkBuilder.slash(savedAccount.getId()).withRel("delete-account");
            URI uri = selfLink.toUri();
            accountModel.add(selfLink, profileLink, getAccountsLink, updateLink, deleteLink);

            //인증 이메일 전송
            emailService.sendAuthMessage(savedAccount);
            return ResponseEntity.created(uri).body(accountModel);
        } catch (IOException e) {//파일 생성 실패
            e.printStackTrace();
            errors.reject("imageFile", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorsModel(errors));
        } catch (IllegalArgumentException e) {//multipart content type이 image가 아닌 경우
            e.printStackTrace();
            errors.reject("imageFile", "You have to send only image file.");
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(new ErrorsModel(errors));
        } catch (MessagingException e) {//이메일 전송 실패 시
            e.printStackTrace();
            errors.reject("email", "fail to send Auth Email.");
            accountService.delete(savedAccount);//저장된 사용자 정보 roll back
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorsModel(errors));
        }
    }

    //이메일 인증 핸들러
    @GetMapping("/accounts/{accountId}/authenticateEmail")
    public ModelAndView authenticateEmail(@PathVariable("accountId") Account account,
                                          @RequestParam String code) {

        ModelAndView modelAndView = new ModelAndView();
        if (account == null) {
            modelAndView.setStatus(HttpStatus.NOT_FOUND);
            return modelAndView;
        }
        modelAndView.setViewName("authenticate-email");
        if (account.isEmailAuth()) {
            modelAndView.addObject("result", "이미 인증이 완료되었습니다. traveler를 마음껏 즐겨주세요!");
            return modelAndView;
        }

        if (!account.getAuthCode().equals(code)) {
            modelAndView.addObject("result", "유효하지 않은 인증 코드입니다. 인증 코드를 확인해주세요.");
            return modelAndView;
        }

        accountService.updateEmailAuth(account);//이메일 인증이 완료된 계정으로 update
        modelAndView.addObject("result", "인증이 완료되었습니다. traveler를 마음껏 즐겨주세요!");
        return modelAndView;
    }

    //사용자 목록 조회 핸들러
    @GetMapping("/api/accounts")
    public ResponseEntity getAccounts(Pageable pageable,
                                      PagedResourcesAssembler<Account> assembler,
                                      @CurrentAccount Account account,
                                      @RequestParam Map<String, String> params) {
        String filter = params.get("filter");
        String search = params.get("search");
        Page<Account> accounts = accountService.findAccounts(pageable, filter, search);

        //hateoas 적용
        PagedModel<AccountModel> accountModels =
                assembler.toModel(accounts,
                        a -> new AccountModel(a, new Link(appProperties.getBaseUrl() + "/accounts/" + a.getId()).withSelfRel()),
                        linkTo(methodOn(AccountController.class).getAccounts(pageable, assembler, account, params)).withSelfRel());
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getGetAccountsAnchor()).withRel("profile");//profile 링크
        accountModels.add(profileLink);

        if (account == null) {//미인증 상태인 경우 계정 생성 링크 추가
            Link createAccountLink = linkTo(methodOn(AccountController.class).getAccounts(pageable, assembler, account, params)).withRel("create-account");
            accountModels.add(createAccountLink);
        }
        return ResponseEntity.ok(accountModels);
    }

    //사용자 한 명 조회 핸들러
    @GetMapping("/api/accounts/{accountId}")
    public ResponseEntity getAccount(@PathVariable("accountId") Account targetAccount,
                                     @CurrentAccount Account account) {
        if (targetAccount == null || !targetAccount.isEmailAuth()) {//존재하지 않는 사용자이거나 이메일 인증이 되지 않은 사용자인 경우
            return ResponseEntity.notFound().build();
        }

        //Hateoas적용
        Link link = new Link(appProperties.getBaseUrl() + "/api/accounts/" + targetAccount.getId());
        AccountModel accountModel = new AccountModel(targetAccount, link.withSelfRel());
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getGetAccountAnchor()).withRel("profile");//profile 링크
        Link getAccountsLink = new Link(appProperties.getBaseUrl() + "/api/accounts").withRel("get-accounts");
        accountModel.add(profileLink, getAccountsLink);

        if (account != null) {
            if (account.equals(targetAccount)) {//인증 상태에서 자기 자신을 조회한 경우
                Link updateAccountLink = link.withRel("update-account");
                Link deleteAccountLink = link.withRel("delete-account");
                accountModel.add(updateAccountLink, deleteAccountLink);
            } else {//인증 상태에서 다른 사용자를 조회한 경우
                Optional<Follow> followOpt = followService.getFollow(account, targetAccount);
                if (followOpt.isEmpty()) {//조회한 사용자를 팔로잉하고 있지 않은 경우
                    Link followLink = new Link(appProperties.getBaseUrl() + "/api/accounts/" + account.getId() + "/followings").withRel("create-account-following");
                    accountModel.add(followLink);
                } else {//조회한 사용자를 팔로잉하고 있는 경우
                    Link unfollowLink = new Link(appProperties.getBaseUrl() + "/api/accounts/" + account.getId() + "/followings/" + targetAccount.getId()).withRel("delete-account-following");
                    accountModel.add(unfollowLink);
                }
            }
        }

        return ResponseEntity.ok(accountModel);
    }

    //사용자 리소스 수정
    @PostMapping("/api/accounts/{accountId}")
    public ResponseEntity updateAccount(@PathVariable("accountId") Account targetAccount,
                                        @RequestPart("imageFile") List<MultipartFile> imageFile,
                                        @RequestPart("account") @Valid AccountDtoForUpdate accountDtoForUpdate,
                                        Errors errors,
                                        @CurrentAccount Account account) {
        if (targetAccount == null) {//존재하지 않는 사용자인 경우
            return ResponseEntity.notFound().build();
        }

        if (!targetAccount.equals(account)) {//다른 사용자인 경우
            errors.reject("forbidden", "You can not update other user's information.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorsModel(errors));
        }

        if (errors.hasErrors()) {//account part의 값이 유효하지 않은 값인 경우
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }


        if (imageFile.size() >= 2) {//프로필 이미지가 2개 이상이 넘어오는 경우
            errors.reject("imageFile", "Max image count is 1.");
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        modelMapper.map(accountDtoForUpdate, targetAccount);
        try {
            //hateoas적용
            Account savedAccount = accountService.update(targetAccount, imageFile);
            Link link = new Link(appProperties.getBaseUrl() + "/api/accounts/" + targetAccount.getId());
            AccountModel accountModel = new AccountModel(savedAccount, link.withSelfRel());
            Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getUpdateAccountAnchor()).withRel("profile");//profile 링크
            Link getAccountsLink = new Link(appProperties.getBaseUrl() + "/api/accounts").withRel("get-accounts");
            Link deleteAccountLink = link.withRel("delete-account");
            accountModel.add(profileLink, getAccountsLink, deleteAccountLink);
            return ResponseEntity.ok(accountModel);
        } catch (IOException e) {//파일 생성 실패
            e.printStackTrace();
            errors.reject("imageFile", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorsModel(errors));
        } catch (IllegalArgumentException e) {//multipart content type이 image가 아닌 경우
            e.printStackTrace();
            errors.reject("imageFile", "You have to send only image file.");
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(new ErrorsModel(errors));
        }
    }

    @DeleteMapping("/api/accounts/{accountId}")
    public ResponseEntity deleteAccount(@PathVariable("accountId") Account targetAccount,
                                        @CurrentAccount Account account) {
        if (targetAccount == null) {//존재하지 않는 사용자인 경우
            return ResponseEntity.notFound().build();
        }

        if (!targetAccount.equals(account)) {//다른 사용자인 경우
            Errors errors = new DirectFieldBindingResult(account, "account");
            errors.reject("forbidden", "You can not delete other user resource.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorsModel(errors));
        }

        accountService.delete(targetAccount);//사용자 리소스 제거
        return ResponseEntity.noContent().build();
    }
}
