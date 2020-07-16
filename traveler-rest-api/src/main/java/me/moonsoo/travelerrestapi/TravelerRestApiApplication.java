package me.moonsoo.travelerrestapi;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import me.moonsoo.travelerrestapi.properties.S3Properties;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@SpringBootApplication(scanBasePackages = {"me.moonsoo.commonmodule", "me.moonsoo.travelerrestapi"})
public class TravelerRestApiApplication {

    //실제 서비스시의 프로퍼티
    public static final String properties =
            "spring.config.location=" +
                    "classpath:application.properties," +
                    "classpath:datasource.properties," +
                    "classpath:aws.properties," +
                    "classpath:email.properties"
            ;

    //테스트 프로파일 시의 프로퍼티
    public static final String testProperties =
            "spring.config.location=" +
                    "classpath:application.properties," +
                    "classpath:datasource-test.properties," +
                    "classpath:aws.properties," +
                    "classpath:email.properties"
            ;

    public static void main(String[] args) {
        String profile = args[0];
        SpringApplicationBuilder springApplicationBuilder = new SpringApplicationBuilder(TravelerRestApiApplication.class);
        if(profile.equals("--spring.profiles.active=test")) {
            springApplicationBuilder.properties(testProperties);
        }
        else {
            springApplicationBuilder.properties(properties);
        }
        springApplicationBuilder.run(args);
    }


    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public JPAQueryFactory queryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
