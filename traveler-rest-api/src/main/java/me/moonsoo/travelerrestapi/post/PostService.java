package me.moonsoo.travelerrestapi.post;


import me.moonsoo.commonmodule.account.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

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


}
