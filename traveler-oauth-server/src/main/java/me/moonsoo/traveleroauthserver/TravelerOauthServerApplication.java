package me.moonsoo.traveleroauthserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = {"me.moonsoo.traveleroauthserver", "me.moonsoo.commonmodule"})
public class TravelerOauthServerApplication {

    //실제 서비스시의 프로퍼티
    public static final String properties =
            "spring.config.location=" +
                    "classpath:datasource.properties" +
                    "classpath:application.properties";

    //테스트 프로파일 시의 프로퍼티
    public static final String testProperties =
            "spring.config.location=" +
                    "classpath:application.properties," +
                    "classpath:datasource-test.properties";

    public static void main(String[] args) {
        String profile = (args.length == 0) ? null : args[0];
        SpringApplicationBuilder springApplicationBuilder = new SpringApplicationBuilder(TravelerOauthServerApplication.class);
        if (profile != null && profile.equals("--spring.profiles.active=test")) {
            springApplicationBuilder.properties(testProperties);
        } else {
            springApplicationBuilder.properties(properties);
        }
        springApplicationBuilder.run(args);
    }

}
