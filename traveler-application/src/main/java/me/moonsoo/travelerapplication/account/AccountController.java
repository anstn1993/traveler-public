package me.moonsoo.travelerapplication.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.Sex;
import me.moonsoo.travelerapplication.email.EmailService;
import me.moonsoo.travelerapplication.error.ForbiddenException;
import me.moonsoo.travelerapplication.error.PageNotFoundException;
import me.moonsoo.travelerapplication.deserialize.CustomDeserializer;
import me.moonsoo.travelerapplication.deserialize.CustomPagedModel;
import me.moonsoo.travelerapplication.properties.AppProperties;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Controller
public class AccountController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OAuth2RestTemplate oAuth2RestTemplate;

    @Autowired
    private AccountService accountService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountValidator validator;

    @Autowired
    private AccountValidatorForUpdate validatorForUpdate;

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String getLoginPage(Model model, @ModelAttribute("message") String message, @SessionAttribute(required = false) SessionAccount account, @CookieValue(required = false) String username) {
        //이미 로그인 상태인 경우
        if (account != null) {
            return "redirect:/";
        }

        if (!message.equals("")) {
            model.addAttribute("message", message);
        }

        //사용자 계정이 쿠키로 존재하는 경우
        if (username != null) {
            model.addAttribute("username", username);
        }
        return "account/login";
    }


    @GetMapping("/logout")
    public String getLogoutPage() {
        return "account/logout";
    }

    @GetMapping("/find-username")
    public String getFindUsernamePage(@SessionAttribute(required = false) SessionAccount account,
                                      Model model,
                                      @ModelAttribute("message") String message) {
        if (account != null) {//로그인 상태인 경우
            return "redirect:/";
        }

        if (!message.equals("")) {
            model.addAttribute("message", message);
        }
        return "account/find-username";
    }


    //사용자 이름과 이메일 주소를 받아서 존재하는 사용자인지 검사하고
    //존재하는 사용자인 경우 인증 코드를 생성하여 사용자 메일로 전송하고 인증코드와 사용자 아이디를 세션에 저장하는 핸들러
    @PostMapping("/find-username")
    public String createAuthCodeAndSendMailForUsername(@SessionAttribute(required = false) SessionAccount account,
                                                       @RequestParam String name,
                                                       @RequestParam String email,
                                                       RedirectAttributes redirectAttributes,
                                                       HttpSession session) {
        if (account != null) {//로그인 상태인 경우
            return "redirect:/";
        }
        Optional<Account> targetAccountOpt = accountService.findByNameAndEmail(name, email);//사용자로부터 받은 값으로 조회한 사용자
        if (targetAccountOpt.isEmpty()) {//사용자 fetch가 되지 않은 경우
            redirectAttributes.addAttribute("message", "이름과 이메일을 다시 확인해주세요.");
            return "redirect:/find-username";
        }

        try {
            String authCode = emailService.sendAuthMessage(targetAccountOpt.get());//인증 메일 전송
            //사용자가 입력한 인증 번호와 비교하기 위해서 세션에 인증 코드를 저장한다.
            session.setAttribute("authCode", authCode);
            //인증 타입을 설정하여 인증 후의 로직을 처리
            session.setAttribute("authType", "username");
            //사용자가 인증에 성공했을 시 사용자 아이디를 반환해주기 위해서 username을 세션에 미리 저장해둔다.
            session.setAttribute("username", targetAccountOpt.get().getUsername());
            //인증 요청 페이지에서
            return "redirect:/authenticate";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addAttribute("message", "서버에 문제가 생겼습니다. 잠시 후 다시 시도해주세요.");
            return "redirect:/find-username";
        }
    }

    @GetMapping("/find-password")
    public String getFindPasswordPage(@SessionAttribute(required = false) SessionAccount account,
                                      Model model,
                                      @ModelAttribute("message") String message) {
        if (account != null) {//로그인 상태인 경우
            return "redirect:/";
        }

        if (!message.equals("")) {
            model.addAttribute("message", message);
        }
        return "account/find-password";
    }

    //사용자 아이디와 이메일 주소를 받아서 존재하는 사용자인지 검사하고
    //존재하는 사용자인 경우 인증 코드를 생성하여 사용자 메일로 전송하고 세션에 인증코드를 저장하는 핸들러
    @PostMapping("/find-password")
    public String createAuthCodeAndSendMailForPassword(@SessionAttribute(required = false) SessionAccount account,
                                                       @RequestParam String username,
                                                       @RequestParam String email,
                                                       RedirectAttributes redirectAttributes,
                                                       HttpSession session) {
        if (account != null) {//로그인 상태인 경우
            return "redirect:/";
        }
        Optional<Account> targetAccountOpt = accountService.findByUsernameAndEmail(username, email);//사용자로부터 받은 값으로 조회한 사용자
        if (targetAccountOpt.isEmpty()) {//사용자 fetch가 되지 않은 경우
            redirectAttributes.addAttribute("message", "아이디와 이메일을 다시 확인해주세요.");
            return "redirect:/find-password";
        }

        try {
            String authCode = emailService.sendAuthMessage(targetAccountOpt.get());//인증 메일 전송
            //사용자가 입력한 인증 번호와 비교하기 위해서 세션에 인증 코드를 저장한다.
            session.setAttribute("authCode", authCode);
            //인증 타입을 설정하여 인증 후의 로직을 처리
            session.setAttribute("authType", "password");
            session.setAttribute("username", targetAccountOpt.get().getUsername());
            //인증 요청 페이지에서
            return "redirect:/authenticate";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addAttribute("message", "서버에 문제가 생겼습니다. 잠시 후 다시 시도해주세요.");
            return "redirect:/find-password";
        }
    }

    //인증 페이지 get
    @GetMapping("/authenticate")
    public String getAuthenticatePage(@SessionAttribute(required = false) SessionAccount account,
                                      @SessionAttribute(required = false) String authCode,
                                      HttpSession session) {
        if (account != null) {//로그인 상태인 경우
            return "redirect:/";
        }

        if (authCode == null) {//세션에 인증 코드가 없는 상태에서 요청한 경우
            return "redirect:/";
        }
        return "account/authenticate";
    }

    //param: code-사용자가 입력한 코드, authCode-세션에 저장되어 있는 인증 코드
    //사용자가 입력한 인증코드를 받아서 세션에 저장된 인증 코드와 비교하여 일치, 불일치 여부에 따라 다른 응답을 하는 핸들러
    @PostMapping("/authenticate")
    public ResponseEntity authenticateUserForUsername(@SessionAttribute(required = false) String authCode,
                                                      @RequestParam("authCode") String code,
                                                      HttpSession session) {
        if (!authCode.equals(code)) {//사용자가 입력한 코드와 인증코드가 일치하지 않는 경우
            return ResponseEntity.badRequest().build();
        } else {//사용자가 입력한 코드와 인증코드가 일치하는 경우
            //인증 타입을 응답 본문에 추가한다.
            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("authType", (String) session.getAttribute("authType"));
            return ResponseEntity.ok(objectNode);
        }
    }

    //사용자 아이디 찾기 페이지 get
    @GetMapping("/find-username/result")
    public String returnUsername(@SessionAttribute(required = false) SessionAccount account,
                                 @SessionAttribute(required = false) String username,
                                 Model model,
                                 HttpSession session) {
        if (account != null) {//이미 로그인 상태인 경우
            return "redirect:/";
        }

        //인증 코드가 세션에 존재한다는 건 아직 인증을 하지 못 했다는 것을 의미하기 때문에
        //메인 페이지로 redirect
        if (username == null) {
            return "redirect:/";
        }
        model.addAttribute("username", username);
        session.invalidate();
        return "account/find-username-result";
    }

    //비밀번호 변경 페이지 get
    @GetMapping("/find-password/result")
    public String returnPassword(@SessionAttribute(required = false) SessionAccount account,
                                 @SessionAttribute(required = false) String authCode,
                                 HttpSession session) {
        if (account != null) {//이미 로그인 상태인 경우
            return "redirect:/";
        }

        //인증 코드가 세션에 존재한다는 건 아직 인증을 하지 못 했다는 것을 의미하기 때문에
        //메인 페이지로 redirect
        if (authCode == null) {
            return "redirect:/";
        }
        session.removeAttribute("authType");
        session.removeAttribute("authCode");
        return "account/find-password-result";
    }

    //비밀번호 변경 처리
    @PostMapping("/find-password/result")
    public ResponseEntity setUpPassword(@RequestParam String password,
                                        @SessionAttribute(required = false) String username,
                                        HttpSession session) {
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        accountService.updatePassword(username, password);
        session.invalidate();
        return ResponseEntity.ok().build();
    }


    //사용자 인증 페이지에서 벗어나거나 새로고침을 하는 경우 인증 초기화 여부를 세션에서 삭제하기 위한 핸들러
    @PostMapping("/invalidAuthCode")
    public ResponseEntity invalidAuthCode(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sign-up")
    public String getSignUpPage(@SessionAttribute(required = false) SessionAccount account) {
        if (account != null) {
            return "redirect:/";
        }
        return "account/sign-up";
    }

    //회원가입 처리
    @PostMapping("/sign-up")
    public ResponseEntity signUp(@RequestPart(required = false) MultipartFile imageFile, @RequestParam Map<String, String> params) throws IOException {
        //필요한 request parameter가 모두 넘어오지 않은 경우 bad request response
        if (!hasAllParamsForSignUp(params)) {
            ObjectNode error = objectMapper.createObjectNode();
            error.put("error", "You didn't send all request parameters for sign up!");
            return ResponseEntity.badRequest().body(error);
        }
        AccountDto accountDto = bindAccountDto(params);
        Errors errors = new BeanPropertyBindingResult(accountDto, "accountDto");
        validator.validate(accountDto, errors);
        //폼 데이터가 유효하지 않은 경우 bad request response
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        //multipart form
        MultiValueMap<String, Object> multiPart = createSignUpMultipart(imageFile, accountDto);
        HttpHeaders requestHeader = new HttpHeaders();
        requestHeader.setAccept(List.of(MediaTypes.HAL_JSON));
        requestHeader.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(multiPart, requestHeader);
        //traveler rest api로 post요청을 보낸다.
        ResponseEntity<Object> response = restTemplate.postForEntity(appProperties.getRestApiUrl() + appProperties.getCreateAccountUri(),
                request, Object.class);
        return response;
    }



    //사용자 페이지 get
    @GetMapping("/users/{userId}")
    public String getUserPage(@PathVariable("userId") Account targetUser,
                              @SessionAttribute(required = false) SessionAccount account,
                              Model model) {
        if (targetUser == null) {
            throw new PageNotFoundException();
        }
        boolean following = false;//사용자 팔로잉 여부
        if (account != null && !targetUser.getId().equals(account.getId())) {
            //targetUser를 팔로잉하고 있는지 조회
            try {
                ResponseEntity<Object> followingRequest = oAuth2RestTemplate.getForEntity(appProperties.getRestApiUrl() + "/accounts/" + account.getId() + "/followings/" + targetUser.getId(), Object.class);
                if (followingRequest.getStatusCode().equals(HttpStatus.OK)) {
                    following = true;
                }
            } catch (HttpClientErrorException e) {
                e.printStackTrace();
            }
            model.addAttribute("following", following);
        }
        //사용자의 팔로잉, 팔로워 수를 조회하는 요청이다.
        ResponseEntity<Object> followResourceCountRequest = oAuth2RestTemplate.getForEntity(appProperties.getRestApiUrl() + "/accounts/" + targetUser.getId() + "/follow/count", Object.class);
        LinkedHashMap<String, Integer> followResourceCount = (LinkedHashMap<String, Integer>) followResourceCountRequest.getBody();
        model.addAttribute("followingCount", followResourceCount.get("followingCount"));
        model.addAttribute("followerCount", followResourceCount.get("followerCount"));
        model.addAttribute("user", targetUser);//사용자 정보
        return "account/userpage";
    }

    //프로필 수정 페이지 get
    @GetMapping("/users/{userId}/profile")
    public String getProfilePage(@PathVariable("userId") Account targetUser,
                                 @SessionAttribute(required = false) SessionAccount account,
                                 Model model) {
        if (targetUser == null) {
            throw new PageNotFoundException();
        }
        if (account == null || !account.getId().equals(targetUser.getId())) {
            throw new ForbiddenException();
        }
        model.addAttribute("account", account);
        return "account/edit-profile";
    }

    //프로필 수정 처리 핸들러
    @PostMapping("/users/{userId}/profile")
    public ResponseEntity updateProfile(@PathVariable("userId") Account targetUser,
                                        @SessionAttribute(required = false) SessionAccount account,
                                        MultipartFile imageFile,
                                        @RequestParam Map<String, String> params) {
        //로그인하지 않았거나 다른 사용자의 프로필을 수정하려고 하는 경우 403 return
        if (account == null || targetUser == null || !account.getId().equals(targetUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        //필요한 request parameter가 모두 넘어오지 않은 경우 bad request response
        if (!hasAllParamsForEditProfile(params)) {
            ObjectNode error = objectMapper.createObjectNode();
            error.put("error", "You didn't send all request parameters for edit profile!");
            return ResponseEntity.badRequest().body(error);
        }
        AccountDtoForUpdate accountDto = bindAccountDtoForUpdate(params);
        Errors errors = new BeanPropertyBindingResult(accountDto, "accountDto");
        validatorForUpdate.setCurrentNickname(account.getNickname());
        validatorForUpdate.validate(accountDto, errors);
        //폼 데이터가 유효하지 않은 경우
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }
        modelMapper.map(accountDto, targetUser);
        try {
            accountService.update(targetUser, imageFile);//db에 수정 작업 및 프로필 이미지 s3서버로 업데이트
            modelMapper.map(targetUser, account);//세션에 저장된 사용자 정보 update
            return ResponseEntity.ok().build();
        } catch (IOException e) {//파일 생성 실패
            e.printStackTrace();
            errors.reject("imageFile", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errors);
        } catch (IllegalArgumentException e) {//multipart content type이 image가 아닌 경우
            e.printStackTrace();
            errors.reject("imageFile", "You have to send only image file.");
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(errors);
        }
    }

    //비밀번호 수정 핸들러
    @PutMapping("/users/{userId}/password")
    public ResponseEntity updatePassword(@PathVariable("userId") Account targetUser,
                                         @SessionAttribute(required = false) SessionAccount account,
                                         @RequestParam Map<String, String> params) {
        //로그인하지 않았거나 다른 사용자의 프로필을 수정하려고 하는 경우 403 return
        if (account == null || targetUser == null || !account.getId().equals(targetUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        //필요한 request parameter가 모두 넘어오지 않은 경우 bad request response
        if (!hasAllParamsForChangingPassword(params)) {
            ObjectNode error = objectMapper.createObjectNode();
            error.put("error", "You didn't send all request parameters for edit profile!");
            return ResponseEntity.badRequest().body(error);
        }
        //요청 parameter들의 유효성 검사
        Errors errors = new MapBindingResult(params, "password");
        validateParamsForChangingPassword(params, errors, targetUser.getPassword());
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        accountService.updatePassword(targetUser.getUsername(), params.get("new-password"));//비밀번호 수정
        return ResponseEntity.ok().build();
    }

    //회원 탈퇴 핸들러
    @DeleteMapping("/users/{userId}/withdrawl")
    public ResponseEntity deleteAccount(@PathVariable("userId") Account targetUser,
                                        @SessionAttribute SessionAccount account,
                                        HttpSession session) throws OAuth2AccessDeniedException {
        //로그인하지 않았거나 다른 사용자의 프로필을 수정하려고 하는 경우 403 return
        if (account == null || targetUser == null || !account.getId().equals(targetUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        oAuth2RestTemplate.delete(appProperties.getRestApiUrl() + appProperties.getDeleteAccountUri() + targetUser.getId());//rest api서버에 사용자 리소스 삭제 request
        session.invalidate();//세션 만료
        return ResponseEntity.noContent().build();
    }

    private AccountDtoForUpdate bindAccountDtoForUpdate(Map<String, String> params) {
        return AccountDtoForUpdate.builder()
                .name(params.get("name"))
                .nickname(params.get("nickname"))
                .introduce(params.get("introduce").equals("") ? null : params.get("introduce"))
                .sex((params.get("sex").equals("male") ? Sex.MALE : Sex.FEMALE))
                .build();
    }

    private AccountDto bindAccountDto(Map<String, String> params) {
        return AccountDto.builder()
                .username(params.get("username"))
                .email(params.get("email"))
                .password(params.get("password"))
                .passwordCheck(params.get("password-check"))
                .name(params.get("name"))
                .nickname(params.get("nickname"))
                .introduce(null)
                .sex((params.get("sex").equals("male") ? Sex.MALE : Sex.FEMALE))
                .build();
    }

    //모든 request parameter가 넘어왔는지 검사하는 메소드
    private boolean hasAllParamsForSignUp(Map<String, String> params) {
        if (params.get("username") == null ||
                params.get("password") == null ||
                params.get("password-check") == null ||
                params.get("email") == null ||
                params.get("name") == null ||
                params.get("nickname") == null ||
                params.get("sex") == null) {
            return false;
        }
        return true;
    }

    private boolean hasAllParamsForEditProfile(Map<String, String> params) {
        if (params.get("name") == null ||
                params.get("nickname") == null ||
                params.get("sex") == null) {
            return false;
        }
        return true;
    }

    private boolean hasAllParamsForChangingPassword(Map<String, String> params) {
        if (params.get("current-password") == null ||
                params.get("new-password") == null ||
                params.get("new-password-check") == null) {
            return false;
        }
        return true;
    }

    //비밀번호 변경 parameter들의 유효성 검사
    //param3: 실제 사용자 비밀번호
    private void validateParamsForChangingPassword(Map<String, String> params, Errors errors, String userPassword) {
        String currentPassword = params.get("current-password");
        String newPassword = params.get("new-password");
        String newPasswordCheck = params.get("new-password-check");

        //공백 검사
        if (currentPassword.isBlank() || newPassword.isBlank() || newPasswordCheck.isBlank()) {
            errors.rejectValue("password", "password empty or whitespace", "비밀번호에 공백은 들어갈 수 없습니다.");
            return;
        }

        //비밀번호 일치 검사
        if (!passwordEncoder.matches(currentPassword, userPassword)) {
            errors.rejectValue("currentPassword", "wrong password", "비밀번호를 다시 확인해주세요.");
            return;
        }

        //새로운 비밀번호 길이 검사
        if (newPassword.length() < 8 || newPassword.length() > 16) {
            errors.rejectValue("newPassword", "password length", "비밀번호는 8에서 16자 사이로 설정해주세요.");
            return;
        }

        //비밀번호, 비밀번호 확인 일치 검사
        if (!newPassword.equals(newPasswordCheck)) {
            errors.rejectValue("newPasswordCheck", "password check", "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            return;
        }
    }

    //회원가입을 위한 multipart form생성
    private MultiValueMap<String, Object> createSignUpMultipart(MultipartFile imageFile, AccountDto accountDto) throws IOException {
        MultiValueMap<String, Object> multiPart = new LinkedMultiValueMap<>();
        if (imageFile != null) {
            //이미지 파일 part
            HttpHeaders imageFilePartHeader = new HttpHeaders();
            imageFilePartHeader.setContentDispositionFormData("imageFile", imageFile.getOriginalFilename());
            String subType = imageFile.getContentType().split("/")[1];
            MediaType imageType = new MediaType("image", subType);
            imageFilePartHeader.setContentType(imageType);
            HttpEntity<Object> imageFilePart = new HttpEntity<>(imageFile.getBytes(), imageFilePartHeader);
            multiPart.add("imageFile", imageFilePart);
        }

        //사용자 form data part
        HttpHeaders accountPartHeader = new HttpHeaders();
        accountPartHeader.setContentType(MediaType.APPLICATION_JSON);
        accountPartHeader.setContentDispositionFormData("account", "account");
        HttpEntity<AccountDto> accountPart = new HttpEntity<>(accountDto, accountPartHeader);
        multiPart.add("account", accountPart);
        return multiPart;
    }
}
