package me.moonsoo.commonmodule.account;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Set;


@Getter @Setter
@EqualsAndHashCode(of = "id")
@AllArgsConstructor @NoArgsConstructor
@Builder
@Entity
public class Account implements Serializable {
    @Id
    @GeneratedValue
    private Integer id;//pk

    @Column(unique = true)
    @Email
    @NotNull
    private String email;

    @Column(nullable = false)
    @NotNull
    private String password;

    @Column(unique = true)
    private String profileImagePath;

    @Column(nullable = false)
    @NotNull
    private String name;

    @Column(nullable = false, unique = true)
    @NotNull
    private String nickname;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Sex sex;

    private boolean emailAuth;//회원가입 시 이메일 인증 여부

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<AccountRole> roles;//권한

}
