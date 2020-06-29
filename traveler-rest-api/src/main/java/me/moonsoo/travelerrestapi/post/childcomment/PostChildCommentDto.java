package me.moonsoo.travelerrestapi.post.childcomment;

import lombok.*;

import javax.validation.constraints.NotBlank;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostChildCommentDto {

    @NotBlank
    private String comment;

}
