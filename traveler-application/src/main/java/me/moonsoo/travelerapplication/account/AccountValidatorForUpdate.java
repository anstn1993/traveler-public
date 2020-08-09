package me.moonsoo.travelerapplication.account;

import me.moonsoo.commonmodule.account.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Optional;
import java.util.regex.Pattern;

@Component
public class AccountValidatorForUpdate implements Validator {

    @Autowired
    private AccountService accountService;

    private String currentNickname;//현재 사용자 닉네임

    public void setCurrentNickname(String currentNickname) {
        this.currentNickname = currentNickname;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return AccountDtoForUpdate.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        AccountDtoForUpdate accountDto = (AccountDtoForUpdate) target;
        //username, 비밀번호, 이메일, 이름 유효성 검사
        if (!isNameValid(accountDto.getName(), errors) ||
                !isNicknameValid(accountDto.getNickname(), errors) ||
                !isIntroduceValid(accountDto.getIntroduce(), errors)) {
            return;
        }
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
        if(!currentNickname.equals(nickname)) {
            Optional<Account> accountOpt = accountService.findByNickname(nickname);
            if (accountOpt.isPresent()) {
                errors.rejectValue("nickname", "nickname exist", "이미 존재하는 닉네임 입니다.");
                return false;
            }
        }
        return true;
    }

    private boolean isIntroduceValid(String introduce, Errors errors) {
        if(introduce == null || introduce.equals("")) {
            return true;
        }
        //공백 검사
        if (introduce.isBlank()) {
            errors.rejectValue("introduce", "introduce empty or whitespace", "자기소개에 공백은 들어갈 수 없습니다.");
            return false;
        }

        //길이 검사
        if (introduce.length() > 150) {
            errors.rejectValue("introduce", "introduce length", "자기소개는 150자 이내로 설정해주세요.");
            return false;
        }
        return true;
    }
}
