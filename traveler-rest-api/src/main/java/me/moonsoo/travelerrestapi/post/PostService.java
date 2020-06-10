package me.moonsoo.travelerrestapi.post;


import me.moonsoo.commonmodule.account.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class PostService {

    @Autowired
    private FileUploader fileUploader;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostTagRepository postTagRepository;

    @Autowired
    private PostImageRepository postImageRepository;


    public Post save(List<MultipartFile> multipartFileList, Post post, Account account) throws IOException, IllegalArgumentException {

        List<String> uploadedImageUriList = fileUploader.upload(multipartFileList, account);//s3서버로 파일 전송
        post.setAccount(account);
        post.setRegDate(LocalDateTime.now());
        post.setViewCount(0);
        Post savedPost = postRepository.save(post);
        //post image save
        for (String uri : uploadedImageUriList) {
            PostImage postImage = PostImage.builder()
                    .post(savedPost)
                    .uri(uri)
                    .build();
            savedPost.getPostImageList().add(postImage);
            postImageRepository.save(postImage);
        }
        //post tag save
        for (PostTag postTag : savedPost.getPostTagList()) {
            postTag.setPost(savedPost);
            postTagRepository.save(postTag);
        }

        return savedPost;
    }

    //페이징, 검색어 조건에 따른 post 게시물 return
    public Page<Post> findPosts(Pageable pageable, String filter, String search) {
        //검색어와 필터 중 하나라도 유효하지 않은 경우 필터링을 하지 않고 목록 출력
        if (filter == null || filter.isBlank() || search == null || search.isBlank()) {
            return postRepository.findAll(pageable);
        }
        //필터링 조건이 작성자인 경우
        else if (filter.equals("writer")) {
            return postRepository.findAllByAccount_NicknameContains(search, pageable);
        }
        //필터링 조건이 게시물의 본문인 경우
        else if (filter.equals("article")) {
            return postRepository.findAllByArticleContains(search, pageable);
        }
        //필터링 조건이 게시물의 태그인 경우
        else if (filter.equals("tag")) {
            return postRepository.findAllByTagContains(search, pageable);
        }
        //필터링 조건이 장소명인 경우
        else {//filter.equals("location")
            return postRepository.findAllByLocationContains(search, pageable);
        }
    }

    public Post updateViewCount(Post post) {
        post.setViewCount(post.getViewCount() + 1);
        return postRepository.save(post);
    }

    @Transactional
    public void deleteTagAndImage(Post post) {
        List<PostTag> postTagList = postTagRepository.findAllByPost(post);
        List<PostImage> postImageList = postImageRepository.findAllByPost(post);
        postTagRepository.deleteAll(postTagList);//tag db에서 제거
        postImageRepository.deleteAll(postImageList);//이미지 db에서 제거
    }

    public Post updatePost(Post post, Set<PostTag> postTagList, List<MultipartFile> imageFiles) throws IOException, IllegalArgumentException {

        post.setPostTagList(postTagList);//post tag set
        for (PostTag postTag : postTagList) {
            postTag.setPost(post);
            postTagRepository.save(postTag);//tag db에 저장
        }

        fileUploader.delete(post.getPostImageList());//기존 이미지 파일 s3서버에서 제거
        List<String> uploadedImageUriList = fileUploader.upload(imageFiles, post.getAccount());//multipart로 넘어온 이미지 파일 s3서버로 업로드
        Set<PostImage> postImageList = new LinkedHashSet<>();
        //post image set
        for (String uri : uploadedImageUriList) {
            PostImage postImage = PostImage.builder()
                    .post(post)
                    .uri(uri)
                    .build();
            postImageList.add(postImage);
            postImageRepository.save(postImage);
        }
        post.setPostImageList(postImageList);
        return postRepository.save(post);
    }
}
