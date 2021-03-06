package me.moonsoo.travelerrestapi.post;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerrestapi.properties.S3Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class FileUploader {

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private S3Properties s3Properties;

    public List<String> upload(List<MultipartFile> multipartFileList, Account account, String targetDirectory) throws IOException, IllegalArgumentException {

        checkContentType(multipartFileList);
        List<File> tempFiles = convertToFiles(multipartFileList, account);
        List<String> uploadedImageUrlList = uploadToS3(tempFiles, targetDirectory);
        removeTempFiles(tempFiles);
        return uploadedImageUrlList;
    }

    //이미지 파일인지 검사하고 아닐 경우 에러를 던진다.
    private void checkContentType(List<MultipartFile> multipartFileList) throws IllegalArgumentException {
        multipartFileList.forEach(f -> {
            if (!f.getContentType().startsWith("image")) {
                throw new IllegalArgumentException("You can only upload image files.");
            }
        });
    }

    //s3서버로 이미지 파일들 업로드
    private List<String> uploadToS3(List<File> tempFiles, String targetDirectory) {
        List<String> uploadedImageUrlList = new ArrayList<>();
        for (File file : tempFiles) {
            amazonS3.putObject(new PutObjectRequest(s3Properties.getBUCKET(), targetDirectory + "/" + file.getName(), file).withCannedAcl(CannedAccessControlList.PublicRead));//s3로 업로드
            uploadedImageUrlList.add(amazonS3.getUrl(s3Properties.getBUCKET(), targetDirectory + "/" + file.getName()).toString());
        }
        return uploadedImageUrlList;
    }

    //multipart file -> file로 변환
    private List<File> convertToFiles(List<MultipartFile> multipartFileList, Account account) throws IOException {
        List<File> tempFiles = new ArrayList<>();
        for (int i = 0; i < multipartFileList.size(); i++) {
            String extension = getExtension(multipartFileList.get(i).getContentType());//파일의 확장자
            String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());//파일 이름의 무결성을 위해서 현재 시간 + 파일 번호를 부여
            String fileName = account.getUsername() + timeStamp + (i + 1) + "." + extension;//확장자와 time stamp를 합쳐서 파일 이름 생성
            //파일로 변환
            File tempFile = new File(fileName);
            if (tempFile.createNewFile()) {
                try (FileOutputStream fo = new FileOutputStream(tempFile)) {
                    fo.write(multipartFileList.get(i).getBytes());
                }
                tempFiles.add(tempFile);
            }
        }
        return tempFiles;
    }

    //로컬에 저장된 임시 파일들 삭제
    private void removeTempFiles(List<File> imageFiles) {
        for (File imageFile : imageFiles) {
            imageFile.delete();
        }
    }

    //파일의 확장자 추출
    private String getExtension(String contentType) {
        return contentType.split("/")[1];
    }

    //s3서버에 저장된 게시물 이미지 삭제
    public void deletePostImage(Set<PostImage> postImageList) throws AmazonServiceException {
        for (PostImage postImage : postImageList) {
            String[] uriSplit = postImage.getUri().split("/");
            String target = s3Properties.getPostImageDirectory() + "/" + uriSplit[uriSplit.length - 1];
            amazonS3.deleteObject(s3Properties.getBUCKET(), target);
        }
    }

    //s3서버에 저장된 프로필 이미지 삭제
    public void deleteProfileImage(String profileImageUri) {
        String[] uriSplit = profileImageUri.split("/");
        String target = s3Properties.getProfileImageDirectory() + "/" + uriSplit[uriSplit.length - 1];
        amazonS3.deleteObject(s3Properties.getBUCKET(), target);
    }
}
