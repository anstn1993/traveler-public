package me.moonsoo.travelerrestapi.accompany.childcomment;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Builder
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
public class AccompanyChildCommentDto {
    @NotBlank
    private String comment;
}
