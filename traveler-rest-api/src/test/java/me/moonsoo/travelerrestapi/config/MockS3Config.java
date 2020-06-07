package me.moonsoo.travelerrestapi.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import io.findify.s3mock.S3Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class MockS3Config {

    @Value("${cloud.aws.s3.bucket}")
    private String BUCKET;

    @Value("${cloud.aws.region.static}")
    private String REGION;

    @Bean
    public S3Mock s3Mock() {
        return new S3Mock.Builder().withInMemoryBackend().withPort(8001).build();
    }

    @Bean
    public AmazonS3 amazonS3(S3Mock s3Mock) {
        s3Mock.start();
        AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration("http://localhost:8001", REGION);
        AmazonS3 client = AmazonS3ClientBuilder
                .standard()
                .withPathStyleAccessEnabled(true)
                .withEndpointConfiguration(endpoint)
                .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
                .build();
        client.createBucket(BUCKET);
        return client;
    }

}
