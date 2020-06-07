package me.moonsoo.travelerrestapi.post;


import lombok.extern.slf4j.Slf4j;
import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.CurrentAccount;
import me.moonsoo.travelerrestapi.errors.ErrorsModel;
import me.moonsoo.travelerrestapi.properties.AppProperties;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/api/posts")
@Slf4j
public class PostController {

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AppProperties appProperties;

    @Autowired
    PostService postService;

    @PostMapping
    public ResponseEntity createPost(@RequestPart List<MultipartFile> imageFiles,
                                     @RequestPart("post") PostDto postDto,
                                     Errors errors,
                                     @CurrentAccount Account account) {

        //이미지 파일이 함께 넘어왔는지 검사
        if(imageFiles.size() == 0) {
            errors.reject("imageFiles", "You have to include at least one image in the multipart");
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        //이미지 파일이 10개 이상인 경우
        if(imageFiles.size() > 10) {
            errors.reject("imageFiles", "Max Image count is 10.");
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        //비즈니스 로직 유효성 검사
        PostValidator postValidator = new PostValidator();
        postValidator.validate(postDto, errors);
        if(errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        Post post = modelMapper.map(postDto, Post.class);
        Set<PostTag> postTagList = modelMapper.map(postDto.getPostTags(), new TypeToken<Set<PostTag>>(){}.getType());
        post.setPostTagList(postTagList);
        try {
            Post savedPost = postService.save(imageFiles, post, account);
            //hateoas 적용
            PostModel postModel = new PostModel(savedPost);
            WebMvcLinkBuilder linkBuilder = linkTo(PostController.class);
            URI uri = linkBuilder.slash(savedPost.getId()).toUri();
            Link getPostsLink = linkBuilder.withRel("get-posts");//get posts 링크
            Link updatePostLink = linkBuilder.slash(savedPost.getId()).withRel("update-post");
            Link deletePostLink = linkBuilder.slash(savedPost.getId()).withRel("delete-post");
            Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getCreatePostAnchor()).withRel("profile");//profile 링크
            postModel.add(getPostsLink, updatePostLink, deletePostLink, profileLink);
            return ResponseEntity.created(uri).body(postModel);

        } catch (IOException e) {//파일 생성 실패
            e.printStackTrace();
            errors.reject("imageFiles", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorsModel(errors));
        } catch (IllegalArgumentException e) {//multipart content type이 image가 아닌 경우
            e.printStackTrace();
            errors.reject("imageFiles", "You have to send only image files.");
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }
    }
}
