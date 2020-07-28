package me.moonsoo.travelerapplication.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import me.moonsoo.commonmodule.account.Sex;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Builder
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountDto {

    private String username;

    private String password;

    @JsonIgnore
    private String passwordCheck;

    private String email;

    private String name;

    private String nickname;

    private Sex sex;
}
