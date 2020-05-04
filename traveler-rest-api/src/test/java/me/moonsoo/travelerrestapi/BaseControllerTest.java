package me.moonsoo.travelerrestapi;

import ch.qos.logback.classic.spi.EventArgUtil;
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
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.headers.RequestHeadersSnippet;
import org.springframework.restdocs.headers.ResponseHeadersSnippet;
import org.springframework.restdocs.hypermedia.LinksSnippet;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;

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

    //페이징 링크 조각
    protected final LinksSnippet pagingLinks = links(
            linkWithRel("first").description("첫 번째 페이지 리소스 요청 url"),
            linkWithRel("prev").description("이전 페이지 리소스 요청 url"),
            linkWithRel("self").description("현재 페이지 리소스 요청 url"),
            linkWithRel("next").description("다음 페이지 리소스 요청 url"),
            linkWithRel("last").description("마지막 페이지 리소스 요청 url"));

    //request headers 조각
    protected final RequestHeadersSnippet requestHeaders = requestHeaders(
            headerWithName(HttpHeaders.ACCEPT).description("응답 본문으로 받기를 원하는 컨텐츠 타입"),
            headerWithName(HttpHeaders.CONTENT_TYPE).description("요청 본문의 컨텐츠 타입"),
            headerWithName(HttpHeaders.AUTHORIZATION).description("oauth2 access token"));

    protected final ResponseHeadersSnippet responseHeaders = responseHeaders(
            headerWithName(HttpHeaders.CONTENT_TYPE).description("응답 본문의 컨텐츠 타입"));

}
