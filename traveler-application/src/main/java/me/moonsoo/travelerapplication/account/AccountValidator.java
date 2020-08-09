package me.moonsoo.travelerapplication.account;

import me.moonsoo.commonmodule.account.Account;
import org.hibernate.validator.cfg.defs.EmailDef;
import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AccountValidator implements Validator {

    @Autowired
    private AccountService accountService;

    @Override
    public boolean supports(Class<?> clazz) {
        return AccountDto.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        AccountDto accountDto = (AccountDto) target;
        //username, 비밀번호, 이메일, 이름 유효성 검사
        if (!isUsernameValid(accountDto.getUsername(), errors) ||
                !isPasswordValid(accountDto.getPassword(), accountDto.getPasswordCheck(), errors) ||
                !isEmailValid(accountDto.getEmail(), errors) ||
                !isNameValid(accountDto.getName(), errors) ||
                !isNicknameValid(accountDto.getNickname(), errors)) {
            return;
        }
    }

    private boolean isUsernameValid(String username, Errors errors) {
        //공백 검사
        if (username.isBlank()) {
            errors.rejectValue("username", "username empty or whitespace", "아이디에 공백은 들어갈 수 없습니다.");
            return false;
        }

        //길이 검사
        if (username.length() < 4 || username.length() > 16) {
            errors.rejectValue("username", "username length", "아이디는 4자에서 16자 사이로 설정해주세요.");
            return false;
        }

        //중복 검사
        Optional<Account> accountOpt = accountService.findByUsername(username);
        if (accountOpt.isPresent()) {
            errors.rejectValue("username", "username exist", "이미 존재하는 아이디 입니다.");
            return false;
        }

        return true;
    }

    private boolean isPasswordValid(String password, String passwordCheck, Errors errors) {
        //공백 검사
        if (password.isBlank()) {
            errors.rejectValue("password", "password empty or whitespace", "비밀번호에 공백은 들어갈 수 없습니다.");
            return false;
        }

        //길이 검사
        if (password.length() < 8 || password.length() > 16) {
            errors.rejectValue("password", "password length", "비밀번호는 8에서 16자 사이로 설정해주세요.");
            return false;
        }

        //비밀번호, 비밀번호 확인 일치 검사
        if (!password.equals(passwordCheck)) {
            errors.rejectValue("password", "password check", "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            return false;
        }
        return true;
    }

    private boolean isEmailValid(String email, Errors errors) {
        //공백 검사
        if (email.isBlank()) {
            errors.rejectValue("email", "email empty or whitespace", "이메일에 공백은 들어갈 수 없습니다.");
            return false;
        }

        //이메일 양식 검사
        String regex = "^([_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{1,6}))?$";
        if (!Pattern.matches(regex, email)) {
            errors.rejectValue("email", "email regex", "이메일 주소가 유효하지 않습니다.");
            return false;
        }

        //이메일 중복 검사
        Optional<Account> accountOpt = accountService.findByEmail(email);
        if (accountOpt.isPresent()) {
            errors.rejectValue("email", "email exist", "이미 존재하는 이메일 입니다.");
            return false;
        }
        return true;
    }

    private boolean isNameValid(String name, Errors errors) {
        //공백 검사
        if (name.isBlank()) {
            errors.rejectValue("name", "name empty or whitespace", "이름에 공백은 들어갈 수 없습니다.");
            return false;
        }

        //길이 검사
        if (name.length() < 2 || name.length() > 30) {
            errors.rejectValue("name", "name length", "이름은 2에서 30자 사이로 설정해주세요.");
            return false;
        }

        //양식 검사
        String regex = "^[가-힝]{2,}$";
        if (!Pattern.matches(regex, name)) {
            errors.rejectValue("name", "name regex", "이름이 유효하지 않습니다.");
            return false;
        }
        return true;
    }

    private boolean isNicknameValid(String nickname, Errors errors) {
        //공백 검사
        if (nickname.isBlank()) {
            errors.rejectValue("nickname", "nickname empty or whitespace", "이름에 공백은 들어갈 수 없습니다.");
            return false;
        }

        //길이 검사
        if (nickname.length() < 1 || nickname.length() > 20) {
            errors.rejectValue("nickname", "nickname length", "이름은 2에서 30자 사이로 설정해주세요.");
            return false;
        }

        //닉네임 중복 검사
        Optional<Account> accountOpt = accountService.findByNickname(nickname);
        if (accountOpt.isPresent()) {
            errors.rejectValue("nickname", "nickname exist", "이미 존재하는 닉네임 입니다.");
            return false;
        }
        return true;
    }
}
