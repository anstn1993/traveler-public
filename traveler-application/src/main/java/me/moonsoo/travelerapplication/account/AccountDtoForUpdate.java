package me.moonsoo.travelerapplication.account;

import lombok.*;
import me.moonsoo.commonmodule.account.Sex;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountDtoForUpdate {
    private String name;

    private String nickname;

    private String introduce;

    private Sex sex;
}
