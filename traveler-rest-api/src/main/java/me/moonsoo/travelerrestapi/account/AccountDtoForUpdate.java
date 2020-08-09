package me.moonsoo.travelerrestapi.account;


import lombok.*;
import me.moonsoo.commonmodule.account.Sex;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class AccountDtoForUpdate {

    @NotBlank
    @Length(min = 8, max = 16)
    private String password;

    @Column(nullable = false)
    @NotBlank
    @Length(min = 2, max = 30)
    private String name;

    @NotBlank
    @Length(min = 1, max = 20)
    private String nickname;

    @Length(max = 150)
    private String introduce;

    @NotNull
    private Sex sex;
}
