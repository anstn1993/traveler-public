package me.moonsoo.travelerrestapi.account;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerrestapi.email.EmailService;
import me.moonsoo.travelerrestapi.errors.ErrorsModel;
import me.moonsoo.travelerrestapi.properties.AppProperties;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.util.List;

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

    @PostMapping("/api/accounts")
    public ResponseEntity createAccount(@RequestPart List<MultipartFile> imageFile,
                                        @RequestPart("account") @Valid AccountDto accountDto,
                                        Errors errors) {

        if(errors.hasErrors()) {
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
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        } catch (MessagingException e) {//이메일 전송 실패 시
            e.printStackTrace();
            errors.reject("email", "fail to send Auth Email.");
            accountService.delete(savedAccount);//저장된 사용자 정보 roll back
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorsModel(errors));
        }
    }

    @GetMapping("/accounts/{accountId}/authenticateEmail")
    public ModelAndView authenticateEmail(@PathVariable("accountId") Account account,
                                          @RequestParam String code) {

        ModelAndView modelAndView = new ModelAndView();
        if(account == null) {
            modelAndView.setStatus(HttpStatus.NOT_FOUND);
            return modelAndView;
        }
        modelAndView.setViewName("authenticate-email");
        if(account.isEmailAuth()) {
            modelAndView.addObject("result", "이미 인증이 완료되었습니다. traveler를 마음껏 즐겨주세요!");
            return modelAndView;
        }

        if(!account.getAuthCode().equals(code)) {
            modelAndView.addObject("result", "유효하지 않은 인증 코드입니다. 인증 코드를 확인해주세요.");
            return modelAndView;
        }

        accountService.updateEmailAuth(account);//이메일 인증이 완료된 계정으로 update
        modelAndView.addObject("result", "인증이 완료되었습니다. traveler를 마음껏 즐겨주세요!");
        return modelAndView;
    }
}
