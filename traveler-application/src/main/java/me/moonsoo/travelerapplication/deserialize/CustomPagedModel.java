package me.moonsoo.travelerapplication.deserialize;

import lombok.*;
import org.springframework.hateoas.Link;

import java.util.List;

//rest api서버로부터 받아온 json 데이터를 deserialize하기 위한 model 클래스
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomPagedModel<T> {
    private List<T> content;
    private Page page;
    private List<Link> links;
}
