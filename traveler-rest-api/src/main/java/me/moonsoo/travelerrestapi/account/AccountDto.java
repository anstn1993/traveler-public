package me.moonsoo.travelerrestapi.account;

import lombok.*;
import me.moonsoo.commonmodule.account.Sex;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.validation.constraints.*;

@Builder
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class AccountDto {


    @NotBlank
    @Length(min = 4, max = 16)
    @Pattern(regexp = "^[a-zA-Z0-9]*$")
    private String username;

    @NotBlank
    @Length(min = 8, max = 16)
    private String password;

    @Email
    @NotBlank
    private String email;

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
