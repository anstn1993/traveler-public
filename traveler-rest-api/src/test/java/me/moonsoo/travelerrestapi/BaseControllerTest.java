package me.moonsoo.travelerrestapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.moonsoo.travelerrestapi.config.AuthServerConfig;
import me.moonsoo.travelerrestapi.config.ResourceServerConfig;
import me.moonsoo.travelerrestapi.config.RestDocsConfig;
import me.moonsoo.travelerrestapi.config.SecurityConfig;
import org.junit.jupiter.api.Disabled;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Slf4j
@Import({AuthServerConfig.class, ResourceServerConfig.class, SecurityConfig.class, RestDocsConfig.class})
@Disabled
public class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected ModelMapper modelMapper;

}
