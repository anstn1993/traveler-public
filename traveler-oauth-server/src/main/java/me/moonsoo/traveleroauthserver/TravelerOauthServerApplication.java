package me.moonsoo.traveleroauthserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = {"me.moonsoo.traveleroauthserver", "me.moonsoo.commonmodule"})
public class TravelerOauthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TravelerOauthServerApplication.class, args);
    }

}
