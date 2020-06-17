package me.moonsoo.travelerrestapi.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class S3Properties {

    @Value("${cloud.aws.credentials.access-key}")
    private String ACCESS_KEY;

    @Value("${cloud.aws.credentials.secret-key}")
    private String SECRET_KEY;

    @Value("${cloud.aws.s3.bucket}")
    private String BUCKET;

    @Value("${cloud.aws.region.static}")
    private String REGION;

    @Value("${cloud.aws.s3.post-image-directory}")
    private String postImageDirectory;

    @Value("profile-image")
    private String profileImageDirectory;


}
