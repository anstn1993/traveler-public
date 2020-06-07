package me.moonsoo.travelerrestapi.post;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

//request multi part 데이터를 바인딩할 모델 클래스
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDto {

    private String article;//본문의 제목

    @JsonProperty("tagList")
    private Set<PostTagDto> postTags = new LinkedHashSet<>();//게시물 태그 set

    private String location;//장소

    private Double latitude;//장소 위도

    private Double longitude;//장소 경도

}
