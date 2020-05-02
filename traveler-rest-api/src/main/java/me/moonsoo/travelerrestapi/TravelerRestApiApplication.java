package me.moonsoo.travelerrestapi;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = {"me.moonsoo.commonmodule", "me.moonsoo.travelerrestapi"})
public class TravelerRestApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TravelerRestApiApplication.class, args);
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

}
