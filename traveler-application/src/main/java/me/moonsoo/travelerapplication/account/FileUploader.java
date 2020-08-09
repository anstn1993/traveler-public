package me.moonsoo.travelerapplication.account;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import me.moonsoo.commonmodule.account.Account;

import me.moonsoo.travelerapplication.properties.S3Properties;
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

    public String upload(MultipartFile imageFile, Account account, String targetDirectory) throws IOException, IllegalArgumentException {

        checkContentType(imageFile);
        File tempFile = convertToFiles(imageFile, account);
        String uploadedImageUrl = uploadToS3(tempFile, targetDirectory);
        removeTempFiles(tempFile);
        return uploadedImageUrl;
    }

    //이미지 파일인지 검사하고 아닐 경우 에러를 던진다.
    private void checkContentType(MultipartFile imageFile) throws IllegalArgumentException {
        if (!imageFile.getContentType().startsWith("image")) {
            throw new IllegalArgumentException("You can only upload image files.");
        }
    }

    //s3서버로 이미지 파일들 업로드
    private String uploadToS3(File file, String targetDirectory) {
        amazonS3.putObject(new PutObjectRequest(s3Properties.getBUCKET(), targetDirectory + "/" + file.getName(), file).withCannedAcl(CannedAccessControlList.PublicRead));//s3로 업로드
        return amazonS3.getUrl(s3Properties.getBUCKET(), targetDirectory + "/" + file.getName()).toString();
    }

    //multipart file -> file로 변환
    private File convertToFiles(MultipartFile imageFile, Account account) throws IOException {
        String extension = getExtension(imageFile.getContentType());//파일의 확장자
        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());//파일 이름의 무결성을 위해서 현재 시간 + 파일 번호를 부여
        String fileName = account.getId() + timeStamp + 1 + "." + extension;//확장자와 time stamp를 합쳐서 파일 이름 생성
        //파일로 변환
        File tempFile = new File(fileName);
        if (tempFile.createNewFile()) {
            try (FileOutputStream fo = new FileOutputStream(tempFile)) {
                fo.write(imageFile.getBytes());
            }
        }
        return tempFile;
    }

    //로컬에 저장된 임시 파일들 삭제
    private void removeTempFiles(File imageFile) {
            imageFile.delete();
    }

    //파일의 확장자 추출
    private String getExtension(String contentType) {
        return contentType.split("/")[1];
    }

    //s3서버에 저장된 프로필 이미지 삭제
    public void deleteProfileImage(String profileImageUri) {
        String[] uriSplit = profileImageUri.split("/");
        String target = s3Properties.getProfileImageDirectory() + "/" + uriSplit[uriSplit.length - 1];
        amazonS3.deleteObject(s3Properties.getBUCKET(), target);
    }
}
