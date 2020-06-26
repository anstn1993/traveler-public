package me.moonsoo.travelerrestapi.post.comment;


import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCommentDto {

    @NotBlank
    private String comment;

}
