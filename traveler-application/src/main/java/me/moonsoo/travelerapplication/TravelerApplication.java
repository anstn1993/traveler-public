package me.moonsoo.travelerapplication;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.ui.Model;

@SpringBootApplication(scanBasePackages = {"me.moonsoo.commonmodule", "me.moonsoo.travelerapplication"})
public class TravelerApplication {

    public static final String properties =
            "spring.config.location=" +
                    "classpath:application.properties," +
                    "classpath:datasource.properties," +
                    "classpath:oauth2-client.properties," +
                    "classpath:email.properties";

    public static void main(String[] args) {
        new SpringApplicationBuilder(TravelerApplication.class)
                .properties(properties)
                .run(args);
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
