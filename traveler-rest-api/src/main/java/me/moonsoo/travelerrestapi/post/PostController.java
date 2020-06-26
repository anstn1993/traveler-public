package me.moonsoo.travelerrestapi.post;


import com.amazonaws.AmazonServiceException;
import lombok.extern.slf4j.Slf4j;
import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.CurrentAccount;
import me.moonsoo.travelerrestapi.errors.ErrorsModel;
import me.moonsoo.travelerrestapi.properties.AppProperties;
import me.moonsoo.travelerrestapi.schedule.ScheduleController;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

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

    //post 게시물 생성 핸들러
    @PostMapping
    public ResponseEntity createPost(@RequestPart List<MultipartFile> imageFiles,
                                     @RequestPart("post") PostDto postDto,
                                     Errors errors,
                                     @CurrentAccount Account account) {

        //이미지 파일이 함께 넘어왔는지 검사
        if (imageFiles.isEmpty()) {
            errors.reject("imageFiles", "You have to include at least one image in the multipart");
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        //이미지 파일이 10개 이상인 경우
        if (imageFiles.size() > 10) {
            errors.reject("imageFiles", "Max Image count is 10.");
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        //post part의 비즈니스 로직 유효성 검사
        PostValidator postValidator = new PostValidator();
        postValidator.validate(postDto, errors);
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        Post post = modelMapper.map(postDto, Post.class);
        Set<PostTag> postTagList = modelMapper.map(postDto.getPostTags(), new TypeToken<Set<PostTag>>() {
        }.getType());
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
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(new ErrorsModel(errors));
        }
    }

    //post게시물 목록 조회 핸들러
    @GetMapping
    public ResponseEntity getPosts(Pageable pageable,
                                   PagedResourcesAssembler<Post> assembler,
                                   @RequestParam Map<String, String> params,
                                   @CurrentAccount Account account) {
        String filter = params.get("filter");//검색 필터링 카테고리
        String search = params.get("search");//검색어
        Page<Post> posts = postService.findPosts(pageable, filter, search);

        //hateoas적용
        PagedModel<PostModel> postModels =
                assembler.toModel(posts,
                        p -> new PostModel(p, linkTo(PostController.class).slash(p.getId()).slash("comments").withRel("get-post-comments")),
                        //page링크에 filter, search 같은 request param을 함께 붙이기 위해서 필요한 링크
                        linkTo(methodOn(PostController.class).getPosts(pageable, assembler, params, account)).withSelfRel()
                );
        if (account != null) {//인증 상태에서의 요청인 경우
            Link createPostLink = linkTo(PostController.class).withRel("create-post");
            postModels.add(createPostLink);
        }
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getGetPostsAnchor()).withRel("profile");//profile 링크
        postModels.add(profileLink);
        return ResponseEntity.ok(postModels);
    }

    //post 게시물 하나 조회 핸들러
    @GetMapping("/{postId}")
    public ResponseEntity getPost(@PathVariable("postId") Post post,
                                  @CurrentAccount Account account) {
        if (post == null) {//존재하지 않는 리소스인 경우
            return ResponseEntity.notFound().build();
        }
        Post updatedPost = postService.updateViewCount(post);//조회수 1 증가 처리
        //Hateoas적용
        PostModel postModel = new PostModel(updatedPost);
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getGetPostAnchor()).withRel("profile");//profile 링크
        WebMvcLinkBuilder linkBuilder = linkTo(PostController.class);
        Link getPostsLink = linkBuilder.withRel("get-posts");
        Link getPostCommentsLink = linkBuilder.slash(updatedPost.getId()).slash("comments").withRel("get-post-comments");
        postModel.add(profileLink, getPostsLink, getPostCommentsLink);
        if (account != null && account.equals(updatedPost.getAccount())) {//인증 && 자신의 게시물
            Link updatePostLink = linkBuilder.slash(updatedPost.getId()).withRel("update-post");
            Link deletePostLink = linkBuilder.slash(updatedPost.getId()).withRel("delete-post");
            postModel.add(updatePostLink, deletePostLink);
        }
        return ResponseEntity.ok(postModel);
    }

    //post 게시물 수정 핸들러
    @PostMapping("/{postId}")
    public ResponseEntity updatePost(@PathVariable("postId") Post post,
                                     @RequestPart List<MultipartFile> imageFiles,
                                     @RequestPart("post") PostDto postDto,
                                     Errors errors,
                                     @CurrentAccount Account account) {

        if (post == null) {//존재하지 않는 리소스인 경우
            return ResponseEntity.notFound().build();
        }

        if (!account.equals(post.getAccount())) {//자신의 게시물이 아닌 경우
            errors.reject("forbidden", "You can not update other user's contents.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorsModel(errors));
        }

        if (imageFiles.isEmpty()) {//이미지가 파일이 multipart에 포함되지 않은 경우
            errors.reject("imageFiles", "You have to include at least one image in the multipart");
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        //이미지 파일이 10개 이상인 경우
        if (imageFiles.size() > 10) {
            errors.reject("imageFiles", "Max Image count is 10.");
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        //post part의 비즈니스 로직 유효성 검사
        PostValidator postValidator = new PostValidator();
        postValidator.validate(postDto, errors);
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        Set<PostTag> postTagList = modelMapper.map(postDto.getPostTags(), new TypeToken<Set<PostTag>>() {
        }.getType());
        try {
            Post updatedPost = postService.updatePost(post, postTagList, imageFiles);//post 게시물 update 처리
            PostModel postModel = new PostModel(updatedPost);
            Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getUpdatePostAnchor()).withRel("profile");//profile 링크
            Link getPostsLink = linkTo(PostController.class).withRel("get-posts");
            Link deletePostLink = linkTo(PostController.class).slash(updatedPost.getId()).withRel("delete-post");
            postModel.add(profileLink, getPostsLink, deletePostLink);
            return ResponseEntity.ok(postModel);
        } catch (IOException e) {//파일 생성 실패
            e.printStackTrace();
            errors.reject("imageFiles", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorsModel(errors));
        } catch (IllegalArgumentException e) {//multipart content type이 image가 아닌 경우
            e.printStackTrace();
            errors.reject("imageFiles", "You have to send only image files.");
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(new ErrorsModel(errors));
        } catch (AmazonServiceException e) {
            e.printStackTrace();
            errors.reject("500 error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorsModel(errors));
        }
    }

    //post게시물 삭제 핸들러
    @DeleteMapping("/{postId}")
    public ResponseEntity deletePost(@PathVariable("postId") Post post,
                                     @CurrentAccount Account account) {

        if (post == null) {//존재하지 않는 리소스인 경우
            return ResponseEntity.notFound().build();
        }

        if (!post.getAccount().equals(account)) {//다른 사용자의 게시물인 경우
            Errors errors = new DirectFieldBindingResult(account, "account");
            errors.reject("forbidden", "You can not delete other user's contents.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorsModel(errors));
        }

        //post게시물 삭제 처리
        try {
            postService.delete(post);
            return ResponseEntity.noContent().build();
        } catch (AmazonServiceException e) {
            e.printStackTrace();
            Errors errors = new DirectFieldBindingResult(post, "post");
            errors.reject("500 error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorsModel(errors));
        }
    }
}
