package me.moonsoo.travelerrestapi.post;


import com.amazonaws.AmazonServiceException;
import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerrestapi.post.like.LikeRepository;
import me.moonsoo.travelerrestapi.properties.S3Properties;
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

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    S3Properties s3Properties;

    //post게시물 생성 메소드(이미지 파일을 s3서버에 저장하고 저장에 성공하면 db에 post엔티티 정보들을 저장한다)
    public Post save(List<MultipartFile> multipartFileList, Post post, Account account) throws IOException, IllegalArgumentException {
        try {
            List<String> uploadedImageUriList = fileUploader.upload(multipartFileList, account, s3Properties.getPostImageDirectory());
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
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }
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
    protected void deleteTagAndImage(Post post) {
        List<PostTag> postTagList = postTagRepository.findAllByPost(post);
        List<PostImage> postImageList = postImageRepository.findAllByPost(post);
        postTagRepository.deleteAll(postTagList);//tag db에서 제거
        postImageRepository.deleteAll(postImageList);//이미지 db에서 제거
    }

    //post게시물 update 메소드(s3서버에 저장된 기존의 이미지들을 제거하고 제거에 성공하는 경우 새로운 이미지 파일 업로드 및 엔티티 업데이트 작업 수행)
    public Post updatePost(Post post, Set<PostTag> postTagList, List<MultipartFile> imageFiles) throws IOException, IllegalArgumentException {
        try {
            fileUploader.deletePostImage(post.getPostImageList());//기존 이미지 파일 s3서버에서 제거

            deleteTagAndImage(post);//기존 tag, image를 제거
            post.setPostTagList(postTagList);//새로운 post tag set
            for (PostTag postTag : postTagList) {
                postTag.setPost(post);
                postTagRepository.save(postTag);//tag db에 저장
            }

            List<String> uploadedImageUriList = fileUploader.upload(imageFiles, post.getAccount(), s3Properties.getPostImageDirectory());//multipart로 넘어온 이미지 파일 s3서버로 업로드
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
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        } catch (AmazonServiceException e) {
            e.printStackTrace();
            throw new AmazonServiceException(e.getMessage());
        }
    }

    public void delete(Post post) throws AmazonServiceException{

        try {
            fileUploader.deletePostImage(post.getPostImageList());//s3서버에서 이미지 파일 삭제
            likeRepository.deleteByPost(post);//삭제할 post에 달린 좋아요 리소스 제거
            postRepository.delete(post);//삭제에 성공하면 post 엔티티 delete
        }
        catch (AmazonServiceException e) {
            e.printStackTrace();
            throw new AmazonServiceException(e.getMessage());
        }
    }
}
