package me.moonsoo.travelerrestapi.post;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import javax.persistence.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Entity
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(targetEntity = Post.class)
    @JsonSerialize(using = PostSerializer.class)
    private Post post;

    @Column(nullable = false)
    private String uri;//이미지 uri
}
