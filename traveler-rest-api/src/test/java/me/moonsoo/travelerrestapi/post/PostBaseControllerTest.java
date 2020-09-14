package me.moonsoo.travelerrestapi.post;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerrestapi.BaseControllerTest;
import me.moonsoo.travelerrestapi.post.comment.PostComment;
import me.moonsoo.travelerrestapi.post.comment.PostCommentRepository;
import me.moonsoo.travelerrestapi.properties.S3Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.IntStream;

public class PostBaseControllerTest extends BaseControllerTest {

    @Autowired
    protected ResourceLoader resourceLoader;

    @Autowired
    protected PostRepository postRepository;

    @Autowired
    protected PostImageRepository postImageRepository;

    @Autowired
    protected PostTagRepository postTagRepository;

    @Autowired
    protected PostCommentRepository postCommentRepository;

    @Autowired
    protected AmazonS3 amazonS3;

    @Autowired
    protected S3Properties s3Properties;



    protected Post createPost(Account account, int index, int tagCount, int imageCount) {
        Post post = Post.builder()
                .account(account)
                .article("This is article" + index)
                .location("somewhere" + index)
                .latitude(33.0000)
                .longitude(127.0000)
                .regDate(ZonedDateTime.now())
                .viewCount(0)
                .build();
        Post savedPost = postRepository.save(post);
        //post tag set
        Set<PostTag> postTagList = new LinkedHashSet<>();
        IntStream.range(0, tagCount).forEach(i -> {
            PostTag postTag = createPostTag(i, post);
            postTagList.add(postTag);
        });
        savedPost.setPostTagList(postTagList);

        //post image set
        Set<PostImage> postImageList = new LinkedHashSet<>();
        IntStream.range(0, imageCount).forEach(i -> {
            PostImage postImage = null;
            try {
                postImage = createPostImage(i, savedPost);
            } catch (IOException e) {
                e.printStackTrace();
            }
            postImageList.add(postImage);
        });
        savedPost.setPostImageList(postImageList);
        return savedPost;
    }

    private PostImage createPostImage(int index, Post post) throws IOException {
        String uri = uploadImage(index, post.getAccount());//이미지를 mock s3서버에 업로드
        PostImage postImage = PostImage.builder()
                .uri(uri)
                .post(post)
                .build();
        return postImageRepository.save(postImage);
    }

    private String uploadImage(int index, Account account) throws IOException {
        String targetDirectory = s3Properties.getPostImageDirectory();//이미지를 저장할 디렉토리
        //이미지 파일
        String originalFileName = "2019_Red_Blue_Abstract_Design_Desktop_1366x768.jpg";
        File originalFile = resourceLoader.getResource("classpath:image/" + originalFileName).getFile();
        String imageFileName = account.getId() + new SimpleDateFormat("HHmmss").format(new Date()) + (index + 1) + ".jpg";
        //로컬에 임시 이미지 파일 생성
        File tempFile = new File(imageFileName);
        if (tempFile.createNewFile()) {
            FileCopyUtils.copy(originalFile, tempFile);
        }
        amazonS3.putObject(new PutObjectRequest(s3Properties.getBUCKET(), targetDirectory + "/" + tempFile.getName(), tempFile).withCannedAcl(CannedAccessControlList.PublicRead));//mock s3 bucket에 파일 저장
        tempFile.delete();//로컬에 임시 파일 삭제
        return amazonS3.getUrl(s3Properties.getBUCKET(), targetDirectory + "/" + tempFile.getName()).toString();
    }

    private PostTag createPostTag(int index, Post post) {
        PostTag postTag = PostTag.builder()
                .post(post)
                .tag("tag" + index)
                .build();
        return postTagRepository.save(postTag);
    }

    protected PostComment createPostComment(int index, Account account, Post post) {
        PostComment postComment = PostComment.builder()
                .post(post)
                .account(account)
                .comment("comment" + index)
                .regDate(ZonedDateTime.now())
                .build();

        return postCommentRepository.save(postComment);
    }

}
