package me.moonsoo.travelerrestapi.schedule;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerrestapi.BaseControllerTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ScheduleControllerTest extends BaseControllerTest {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private ScheduleLocationRepository scheduleLocationRepository;

    @Autowired
    private ScheduleDetailRepository scheduleDetailRepository;

    @AfterEach
    public void setUp() {
        scheduleRepository.deleteAll();
        accountRepository.deleteAll();
    }


    @Test
    @DisplayName("일정 게시물 추가")
    public void createSchedule() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        ScheduleDto scheduleDto = createScheduleDto(0, Scope.ALL);//request 요청 본문 dto

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/schedules")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(scheduleDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("title").exists())
                .andExpect(jsonPath("scope").exists())
                .andExpect(jsonPath("regDate").exists())
                .andExpect(jsonPath("viewCount").exists())
                .andExpect(jsonPath("scheduleLocations[0].id").exists())
                .andExpect(jsonPath("scheduleLocations[0].schedule.id").exists())
                .andExpect(jsonPath("scheduleLocations[0].location").exists())
                .andExpect(jsonPath("scheduleLocations[0].latitude").exists())
                .andExpect(jsonPath("scheduleLocations[0].longitude").exists())
                .andExpect(jsonPath("scheduleLocations[0].scheduleDetails[0].id").exists())
                .andExpect(jsonPath("scheduleLocations[0].scheduleDetails[0].scheduleLocation.id").exists())
                .andExpect(jsonPath("scheduleLocations[0].scheduleDetails[0].place").exists())
                .andExpect(jsonPath("scheduleLocations[0].scheduleDetails[0].plan").exists())
                .andExpect(jsonPath("scheduleLocations[0].scheduleDetails[0].startDate").exists())
                .andExpect(jsonPath("scheduleLocations[0].scheduleDetails[0].endDate").exists())
                .andExpect(jsonPath("scheduleLocations[0].id").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.get-schedules").exists())
                .andExpect(jsonPath("_links.update-schedule").exists())
                .andExpect(jsonPath("_links.delete-schedule").exists())
                .andDo(document("create-schedule",
                        links(
                                linkWithRel("self").description("업로드된 일정 게시물의 리소스 링크"),
                                linkWithRel("get-schedules").description("일정 게시물 리스트를 조회할 수 있는 링크"),
                                linkWithRel("update-schedule").description("업로드된 일정 게시물을 수정할 수 있는 링크"),
                                linkWithRel("delete-schedule").description("업로드된 일정 게시물을 삭제할 수 있는 링크"),
                                linkWithRel("profile").description("api 문서 링크")
                        ),
                        requestHeaders,
                        requestFields(
                                fieldWithPath("title").description("일정 게시물의 제목"),
                                fieldWithPath("scope").description("일정 게시물의 공개 범위(NONE, FOLLOWER, ALL)"),
                                fieldWithPath("scheduleLocations[].location").description("여행지명"),
                                fieldWithPath("scheduleLocations[].latitude").description("여행지의 위도"),
                                fieldWithPath("scheduleLocations[].longitude").description("여행지의 경도"),
                                fieldWithPath("scheduleLocations[].scheduleDetails[].place").description("여행지의 세부 장소"),
                                fieldWithPath("scheduleLocations[].scheduleDetails[].plan").description("세부 장소에서 행할 세부적인 계획"),
                                fieldWithPath("scheduleLocations[].scheduleDetails[].startDate").description("세부 장소에서의 일정 시작 시간"),
                                fieldWithPath("scheduleLocations[].scheduleDetails[].endDate").description("세부 장소에서의 일정 종료 시간")
                        ),
                        responseHeaders.and(
                                headerWithName(HttpHeaders.LOCATION).description("업로드한 게시물의 리소스 링크"),
                                headerWithName(HttpHeaders.CONTENT_LENGTH).description("응답 본문 데이터의 크기")
                        ),
                        responseFields(
                                fieldWithPath("id").description("일정 게시물의 id"),
                                fieldWithPath("account.id").description("게시물 작성자의 id"),
                                fieldWithPath("title").description("일정 게시물의 제목"),
                                fieldWithPath("scope").description("일정 게시물의 공개 범위(NONE, FOLLOWER, ALL)"),
                                fieldWithPath("regDate").description("일정 게시물의 작성 시간"),
                                fieldWithPath("viewCount").description("일정 게시물 조회수"),
                                fieldWithPath("scheduleLocations[].id").description("여행지 리소스 id"),
                                fieldWithPath("scheduleLocations[].schedule.id").description("일정 게시물 id"),
                                fieldWithPath("scheduleLocations[].location").description("여행지명"),
                                fieldWithPath("scheduleLocations[].latitude").description("여행지의 위도"),
                                fieldWithPath("scheduleLocations[].longitude").description("여행지의 경도"),
                                fieldWithPath("scheduleLocations[].scheduleDetails[].id").description("여행지의 세부 장소 리소스 id"),
                                fieldWithPath("scheduleLocations[].scheduleDetails[].scheduleLocation.id").description("여행지 리소스 id"),
                                fieldWithPath("scheduleLocations[].scheduleDetails[].place").description("여행지의 세부 장소"),
                                fieldWithPath("scheduleLocations[].scheduleDetails[].plan").description("세부 장소에서 행할 세부적인 계획"),
                                fieldWithPath("scheduleLocations[].scheduleDetails[].startDate").description("세부 장소에서의 일정 시작 시간"),
                                fieldWithPath("scheduleLocations[].scheduleDetails[].endDate").description("세부 장소에서의 일정 종료 시간"),
                                fieldWithPath("_links.self.href").description("업로드된 일정 게시물의 리소스 링크"),
                                fieldWithPath("_links.get-schedules.href").description("일정 게시물 리스트를 조회할 수 있는 링크"),
                                fieldWithPath("_links.update-schedule.href").description("업로드된 일정 게시물을 수정할 수 있는 링크"),
                                fieldWithPath("_links.delete-schedule.href").description("업로드된 일정 게시물을 삭제할 수 있는 링크"),
                                fieldWithPath("_links.profile.href").description("api 문서 링크")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("일정 게시물 추가 실패-요청 본문이 없는 경우(400 Bad request)")
    public void createScheduleFail_Empty_Value() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);

        mockMvc.perform(post("/api/schedules")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("일정 게시물 추가 실패-허용되지 않은 값이 요청 본문에 있는 경우(400 Bad request)")
    public void createScheduleFail_Unknown_Value() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);

        Schedule schedule = createScheduleWithNotAllowedValue(0, Scope.ALL);

        mockMvc.perform(post("/api/schedules")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(schedule)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("일정 게시물 추가 실패-요청 본문이 비즈니스 로직에 맞지 않는 경우(400 Bad request)")
    public void createScheduleFail_Wrong_value() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);

        ScheduleDto scheduleDto = createScheduleDtoWithWrongValue(0, Scope.ALL);

        mockMvc.perform(post("/api/schedules")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(scheduleDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("일정 게시물 추가 실패-요청 헤더에 인증 토큰을 포함하지 않은 경우(401 Unauthorized)")
    public void createScheduleFail_Without_Auth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(email, password, 0);
        ScheduleDto scheduleDto = createScheduleDtoWithWrongValue(0, Scope.ALL);

        mockMvc.perform(post("/api/schedules")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(scheduleDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("인증 상태에서 일정 게시물 목록 조회(자신의 게시물은 scope가 NONE이거나 FOLLOWER더라도 조회, 다른 사용자 게시물은 ALL인 게시물만 조회)-검색어x, 30개의 게시물, 한 페이지에 10개, 2페이지 가져오기")
    public void getSchedules_With_Auth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Account otherAccount = createAccount(email, password, 1);
        //자신의 일정 게시물 15개, 다른 사용자의 일정 게시물 15 생성
        IntStream.range(0, 15).forEach(i -> {
            createSchedule(account, i, 3, 3, Scope.NONE);
            if(i % 2 == 0) {
                createSchedule(otherAccount, i + 15, 3, 3, Scope.ALL);
            }
            else {
                createSchedule(otherAccount, i + 16, 3, 3, Scope.NONE);
            }
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/schedules")
                .header(HttpHeaders.AUTHORIZATION, "Bearer" + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.scheduleList").exists())
                .andExpect(jsonPath("_embedded.scheduleList[0]._links.self").exists())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.first").exists())
                .andExpect(jsonPath("_links.prev").exists())
                .andExpect(jsonPath("_links.next").exists())
                .andExpect(jsonPath("_links.last").exists())
                .andExpect(jsonPath("_links.create-schedule").exists())
        ;
    }

//    @Test
//    @DisplayName("인증 상태에서 일정 게시물 목록 조회(자기 게시물 조회: Scope에 무관하게 모두 query)-검색어x, 30개의 게시물, 한 페이지에 10개, 2페이지 가져오기")
//    public void getSchedules_With_Auth() throws Exception {
//        //Given
//        String email = "anstn1993@email.com";
//        String password = "1111";
//        String accessToken = getAuthToken(email, password, 0);
//        Account otherAccount = createAccount(email, password, 1);
//        //자신의 일정 게시물 15개, 다른 사용자의 일정 게시물 15 생성
//        IntStream.range(0, 15).forEach(i -> {
//            createSchedule(account, i, 3, 3, Scope.NONE);
//            createSchedule(otherAccount, i + 15, 3, 3, Scope.ALL);
//        });
//
//        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/schedules")
//                .header(HttpHeaders.AUTHORIZATION, "Bearer" + accessToken)
//                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
//                .param("page", "1")
//                .param("size", "10")
//                .param("sort", "id,DESC"))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("_embedded.scheduleList").exists())
//                .andExpect(jsonPath("_embedded.scheduleList[0]._links.self").exists())
//                .andExpect(jsonPath("page").exists())
//                .andExpect(jsonPath("_links.self").exists())
//                .andExpect(jsonPath("_links.profile").exists())
//                .andExpect(jsonPath("_links.first").exists())
//                .andExpect(jsonPath("_links.prev").exists())
//                .andExpect(jsonPath("_links.next").exists())
//                .andExpect(jsonPath("_links.last").exists())
//                .andExpect(jsonPath("_links.create-schedule").exists())
//        ;
//    }

    @Test
    @DisplayName("인증 상태에서 일정 게시물 목록 조회(자기 게시물 조회: Scope에 무관하게 모두 query)-검색어 조건: 작성자, 30개의 게시물, 한 페이지에 10개, 2페이지 가져오기")
    public void getSchedules_Filtered_By_Writer_With_Auth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Account otherAccount = createAccount(email, password, 1);
        //자신의 일정 게시물 15개, 다른 사용자의 일정 게시물 15 생성
        IntStream.range(0, 15).forEach(i -> {
            createSchedule(account, i, 3, 3, Scope.NONE);
            createSchedule(otherAccount, i + 15, 3, 3, Scope.ALL);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/schedules")
                .header(HttpHeaders.AUTHORIZATION, "Bearer" + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,DESC")
                .param("filter", "writer")
                .param("search", otherAccount.getNickname()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.scheduleList").exists())
                .andExpect(jsonPath("_embedded.scheduleList[0]._links.self").exists())
                .andExpect(jsonPath("_embedded.scheduleList[1].account.id").value(otherAccount.getId()))
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.first").exists())
                .andExpect(jsonPath("_links.prev").exists())
                .andExpect(jsonPath("_links.last").exists())
                .andExpect(jsonPath("_links.create-schedule").exists())
        ;
    }

    @Test
    @DisplayName("인증 상태에서 일정 게시물 목록 조회(자기 게시물 조회: Scope에 무관하게 모두 query)-검색어 조건: 제목, 30개의 게시물, 한 페이지에 10개, 1페이지 가져오기")
    public void getSchedules_Filtered_By_Title_With_Auth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Account otherAccount = createAccount(email, password, 1);
        //자신의 일정 게시물 15개, 다른 사용자의 일정 게시물 15 생성
        IntStream.range(0, 15).forEach(i -> {
            createSchedule(account, i, 3, 3, Scope.NONE);
            createSchedule(otherAccount, i + 15, 3, 3, Scope.ALL);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/schedules")
                .header(HttpHeaders.AUTHORIZATION, "Bearer" + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "0")
                .param("size", "10")
                .param("sort", "id,DESC")
                .param("filter", "title")
                .param("search", "schedule15"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.scheduleList").exists())
                .andExpect(jsonPath("_embedded.scheduleList[0]._links.self").exists())
                .andExpect(jsonPath("_embedded.scheduleList[0].account.id").value(otherAccount.getId()))
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.create-schedule").exists())
        ;
    }

    @Test
    @DisplayName("인증 상태에서 일정 게시물 목록 조회(자기 게시물 조회: Scope에 무관하게 모두 query)-검색어 조건: 장소, 30개의 게시물, 한 페이지에 10개, 1페이지 가져오기")
    public void getSchedules_Filtered_By_Location_With_Auth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(email, password, 0);
        Account otherAccount = createAccount(email, password, 1);
        //자신의 일정 게시물 15개, 다른 사용자의 일정 게시물 15 생성
        IntStream.range(0, 15).forEach(i -> {
            createSchedule(account, i, 3, 3, Scope.NONE);
            createSchedule(otherAccount, i + 15, 3, 3, Scope.ALL);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/schedules")
                .header(HttpHeaders.AUTHORIZATION, "Bearer" + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "0")
                .param("size", "10")
                .param("sort", "id,DESC")
                .param("filter", "location")
                .param("search", "location0"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.scheduleList").exists())
                .andExpect(jsonPath("_embedded.scheduleList[0]._links.self").exists())
                .andExpect(jsonPath("_embedded.scheduleList[0].account.id").value(otherAccount.getId()))
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.first").exists())
                .andExpect(jsonPath("_links.next").exists())
                .andExpect(jsonPath("_links.last").exists())
                .andExpect(jsonPath("_links.create-schedule").exists())
        ;
    }

    @Test
    @DisplayName("미인증 상태에서 일정 게시물 목록 조회-30개의 게시물(공개범위가 ALL인 게시물 query), 검색어x, 한 페이지에 10개, 2페이지 가져오기")
    public void getSchedulesForAll_Without_Auth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(email, password, 0);
        //일정 게시물 30개 생성
        IntStream.range(0, 30).forEach(i -> {
            createSchedule(account, i, 3, 3, Scope.ALL);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/schedules")
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.scheduleList").exists())
                .andExpect(jsonPath("_embedded.scheduleList[0]._links.self").exists())
                .andExpect(jsonPath("_embedded.scheduleList[0].scope").value("ALL"))
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.first").exists())
                .andExpect(jsonPath("_links.prev").exists())
                .andExpect(jsonPath("_links.next").exists())
                .andExpect(jsonPath("_links.last").exists())
                .andExpect(jsonPath("_links.create-schedule").doesNotHaveJsonPath())
        ;

    }

    @Test
    @DisplayName("미인증 상태에서 일정 게시물 목록 조회-30개의 게시물(공개범위가 ALL인 게시물 query), 검색어 조건: 작성자, 한 페이지에 10개, 2페이지 가져오기")
    public void getSchedulesForAll_Filtered_By_Writer_Without_Auth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(email, password, 0);
        //일정 게시물 30개 생성
        IntStream.range(0, 30).forEach(i -> {
            createSchedule(account, i, 3, 3, Scope.ALL);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/schedules")
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,DESC")
                .param("filter", "writer")
                .param("search", account.getNickname()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.scheduleList").exists())
                .andExpect(jsonPath("_embedded.scheduleList[0]._links.self").exists())
                .andExpect(jsonPath("_embedded.scheduleList[0].scope").value("ALL"))
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.first").exists())
                .andExpect(jsonPath("_links.prev").exists())
                .andExpect(jsonPath("_links.next").exists())
                .andExpect(jsonPath("_links.last").exists())
                .andExpect(jsonPath("_links.create-schedule").doesNotHaveJsonPath())
        ;

    }

    @Test
    @DisplayName("미인증 상태에서 일정 게시물 목록 조회-30개의 게시물(공개범위가 ALL인 게시물 query), 검색어 조건: 제목, 한 페이지에 10개, 1페이지 가져오기")
    public void getSchedulesForAll_Filtered_By_Title_Without_Auth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(email, password, 0);
        //일정 게시물 30개 생성
        IntStream.range(0, 30).forEach(i -> {
            createSchedule(account, i, 3, 3, Scope.ALL);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/schedules")
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "0")
                .param("size", "10")
                .param("sort", "id,DESC")
                .param("filter", "title")
                .param("search", "schedule15"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.scheduleList").exists())
                .andExpect(jsonPath("_embedded.scheduleList[0]._links.self").exists())
                .andExpect(jsonPath("_embedded.scheduleList[0].scope").value("ALL"))
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.create-schedule").doesNotHaveJsonPath())
        ;

    }

    @Test
    @DisplayName("미인증 상태에서 일정 게시물 목록 조회-30개의 게시물(공개범위가 ALL인 게시물 query), 검색어 조건: 장소, 한 페이지에 10개, 1페이지 가져오기")
    public void getSchedulesForAll_Filtered_By_Location_Without_Auth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(email, password, 0);
        //일정 게시물 30개 생성
        IntStream.range(0, 30).forEach(i -> {
            createSchedule(account, i, 3, 3, Scope.ALL);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/schedules")
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "0")
                .param("size", "10")
                .param("sort", "id,DESC")
                .param("filter", "location")
                .param("search", "location0"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.scheduleList").exists())
                .andExpect(jsonPath("_embedded.scheduleList[0]._links.self").exists())
                .andExpect(jsonPath("_embedded.scheduleList[0].scope").value("ALL"))
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.first").exists())
                .andExpect(jsonPath("_links.next").exists())
                .andExpect(jsonPath("_links.last").exists())
                .andExpect(jsonPath("_links.create-schedule").doesNotHaveJsonPath())
        ;

    }

    @Test
    @DisplayName("미인증 상태에서 일정 게시물 목록 조회-30개의 게시물(공개범위가 FOLLWER, NONE인 게시물 query),검색어x, 한 페이지에 10개, 2페이지 가져오기")
    public void getSchedulesForFollower_Or_NONE_Without_Auth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(email, password, 0);
        //공개범위가 FOLLOWER인 게시물 15개, 공개범위가 NONE인 게시물 15개
        IntStream.range(0, 15).forEach(i -> {
            createSchedule(account, i, 3, 3, Scope.FOLLOWER);
            createSchedule(account, i + 15, 3, 3, Scope.NONE);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/schedules")
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.scheduleList").doesNotHaveJsonPath())//미인증상태에서는 공개범위가 FOLLWER, NONE인 게시물은 조회할 수 없다.
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.first").exists())
                .andExpect(jsonPath("_links.prev").exists())
                .andExpect(jsonPath("_links.next").doesNotHaveJsonPath())
                .andExpect(jsonPath("_links.last").exists())
                .andExpect(jsonPath("_links.create-schedule").doesNotHaveJsonPath())
        ;

    }

    @Test
    @DisplayName("미인증 상태에서 일정 게시물 목록 조회-30개의 게시물(공개범위가 FOLLWER, NONE인 게시물 query),검색어 조건: 작성자, 한 페이지에 10개, 2페이지 가져오기")
    public void getSchedulesForFollower_Or_NONE_Filtered_By_Writer_Without_Auth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(email, password, 0);
        //공개범위가 FOLLOWER인 게시물 15개, 공개범위가 NONE인 게시물 15개
        IntStream.range(0, 15).forEach(i -> {
            createSchedule(account, i, 3, 3, Scope.FOLLOWER);
            createSchedule(account, i + 15, 3, 3, Scope.NONE);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/schedules")
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,DESC")
                .param("filter", "writer")
                .param("search", account.getNickname()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.scheduleList").doesNotHaveJsonPath())//미인증상태에서는 공개범위가 FOLLWER, NONE인 게시물은 조회할 수 없다.
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.first").exists())
                .andExpect(jsonPath("_links.prev").exists())
                .andExpect(jsonPath("_links.next").doesNotHaveJsonPath())
                .andExpect(jsonPath("_links.last").exists())
                .andExpect(jsonPath("_links.create-schedule").doesNotHaveJsonPath())
        ;

    }

    @Test
    @DisplayName("미인증 상태에서 일정 게시물 목록 조회-30개의 게시물(공개범위가 FOLLWER, NONE인 게시물 query),검색어 조건: 제목, 한 페이지에 10개, 2페이지 가져오기")
    public void getSchedulesForFollower_Or_NONE_Filtered_By_Title_Without_Auth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(email, password, 0);
        //공개범위가 FOLLOWER인 게시물 15개, 공개범위가 NONE인 게시물 15개
        IntStream.range(0, 15).forEach(i -> {
            createSchedule(account, i, 3, 3, Scope.FOLLOWER);
            createSchedule(account, i + 15, 3, 3, Scope.NONE);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/schedules")
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,DESC")
                .param("filter", "title")
                .param("search", "schedule15"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.scheduleList").doesNotHaveJsonPath())//미인증상태에서는 공개범위가 FOLLWER, NONE인 게시물은 조회할 수 없다.
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.first").exists())
                .andExpect(jsonPath("_links.prev").exists())
                .andExpect(jsonPath("_links.next").doesNotHaveJsonPath())
                .andExpect(jsonPath("_links.last").exists())
                .andExpect(jsonPath("_links.create-schedule").doesNotHaveJsonPath())
        ;

    }

    @Test
    @DisplayName("미인증 상태에서 일정 게시물 목록 조회-30개의 게시물(공개범위가 FOLLWER, NONE인 게시물 query),검색어 조건: 장소, 한 페이지에 10개, 2페이지 가져오기")
    public void getSchedulesForFollower_Or_NONE_Filtered_By_Location_Without_Auth() throws Exception {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(email, password, 0);
        //공개범위가 FOLLOWER인 게시물 15개, 공개범위가 NONE인 게시물 15개
        IntStream.range(0, 15).forEach(i -> {
            createSchedule(account, i, 3, 3, Scope.FOLLOWER);
            createSchedule(account, i + 15, 3, 3, Scope.NONE);
        });

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/schedules")
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,DESC")
                .param("filter", "location")
                .param("search", "location0"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.scheduleList").doesNotHaveJsonPath())//미인증상태에서는 공개범위가 FOLLWER, NONE인 게시물은 조회할 수 없다.
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.first").exists())
                .andExpect(jsonPath("_links.prev").exists())
                .andExpect(jsonPath("_links.next").doesNotHaveJsonPath())
                .andExpect(jsonPath("_links.last").exists())
                .andExpect(jsonPath("_links.create-schedule").doesNotHaveJsonPath())
        ;

    }

    //request 요청 본문으로 binding될 dto객체 생성 메소드
    private ScheduleDto createScheduleDto(int index, Scope scope) {

        AtomicInteger day = new AtomicInteger(1);//schedule detail의 시작 날짜와 종료 날짜의 day에 사용될 변수
        List<ScheduleLocationDto> scheduleLocationDtos = new ArrayList<>();//일정 장소 dto set
        IntStream.range(0, 3).forEach(i -> {
            List<ScheduleDetailDto> scheduleDetailDtos = new ArrayList<>();//상세 일정 dto set
            IntStream.range(0, 3).forEach(j -> {
                ScheduleDetailDto scheduleDetailDto = ScheduleDetailDto.builder()
                        .place("Place" + j + " in location")
                        .plan("Do something in place" + j)
                        .startDate(LocalDateTime.of(2020, 5, day.get() + 1, 12, 0, 0))
                        .endDate(LocalDateTime.of(2020, 5, day.get() + 2, 12, 0, 0))
                        .build();
                scheduleDetailDtos.add(scheduleDetailDto);
                day.getAndIncrement();
            });

            ScheduleLocationDto scheduleLocationDto = ScheduleLocationDto.builder()
                    .location("location" + i)
                    .latitude(33.00000 + i)
                    .longitude(127.00000 + i)
                    .scheduleDetailDtos(scheduleDetailDtos)
                    .build();
            scheduleLocationDtos.add(scheduleLocationDto);
        });

        ScheduleDto scheduleDto = ScheduleDto.builder()
                .title("schedule" + index)
                .scope(scope)
                .scheduleLocationDtos(scheduleLocationDtos)
                .build();

        return scheduleDto;
    }

    //허용되지 않은 값이 포함된 요청 본문을 만드는 메소드
    private Schedule createScheduleWithNotAllowedValue(int index, Scope scope) {

        AtomicInteger day = new AtomicInteger(1);//schedule detail의 시작 날짜와 종료 날짜의 day에 사용될 변수
        List<ScheduleLocation> scheduleLocations = new ArrayList<>();//일정 장소 dto set
        IntStream.range(0, 3).forEach(i -> {
            List<ScheduleDetail> scheduleDetails = new ArrayList<>();//상세 일정 dto set
            IntStream.range(0, 3).forEach(j -> {
                ScheduleDetail scheduleDetail = ScheduleDetail.builder()
                        .id(10)//허용되지 않은 값
                        .place("Place" + j + " in location")
                        .plan("do something in place" + j)
                        .startDate(LocalDateTime.of(2020, 5, day.get() + 1, 12, 0, 0))
                        .endDate(LocalDateTime.of(2020, 5, day.get() + 2, 12, 0, 0))
                        .build();
                scheduleDetails.add(scheduleDetail);
                day.getAndIncrement();
            });


            ScheduleLocation scheduleLocation = ScheduleLocation.builder()
                    .id(10)//허용되지 않은 값
                    .location("location" + i)
                    .latitude(33.00000 + i)
                    .longitude(127.00000 + i)
                    .scheduleDetails(scheduleDetails)
                    .build();
            scheduleLocations.add(scheduleLocation);
        });

        Schedule schedule = Schedule.builder()
                .id(10)//허용되지 않은 값
                .title("schedule" + index)
                .scope(scope)
                .scheduleLocations(scheduleLocations)
                .viewCount(100)//허용되지 않은 값
                .regDate(LocalDateTime.now())//허용되지 않은 값
                .build();

        return schedule;
    }

    //비즈니스 로직에 부합하지 않는 요청 본문 dto 생성 메소드
    private ScheduleDto createScheduleDtoWithWrongValue(int index, Scope scope) {

        List<ScheduleDetailDto> scheduleDetailDtos = new ArrayList<>();//상세 일정 dto set
        IntStream.range(0, 3).forEach(i -> {
            ScheduleDetailDto scheduleDetailDto = ScheduleDetailDto.builder()
                    .place("Place" + i + " in location")
                    .plan("Do something in place" + i)
                    .startDate(LocalDateTime.of(2020, 5, i + 2, 12, 0, 0))
                    .endDate(LocalDateTime.of(2020, 5, i + 1, 12, 0, 0))
                    .build();
            scheduleDetailDtos.add(scheduleDetailDto);
        });

        List<ScheduleLocationDto> scheduleLocationDtos = new ArrayList<>();//일정 장소 dto set
        IntStream.range(0, 3).forEach(i -> {
            ScheduleLocationDto scheduleLocationDto = ScheduleLocationDto.builder()
                    .location("location" + i)
                    .latitude(33.00000 + i)
                    .longitude(127.00000 + i)
                    .scheduleDetailDtos(scheduleDetailDtos)
                    .build();
            scheduleLocationDtos.add(scheduleLocationDto);
        });

        ScheduleDto scheduleDto = ScheduleDto.builder()
                .title("schedule" + index)
                .scope(scope)
                .scheduleLocationDtos(scheduleLocationDtos)
                .build();

        return scheduleDto;
    }

    @Test
    @DisplayName("일정 게시물 추가 메소드 테스트")
    public void createScheduleMethod() {
        //Given
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(email, password, 0);
        Schedule schedule = createSchedule(account, 0, 3, 3, Scope.ALL);
        assertAll(
                () -> assertNotNull(schedule),
                () -> assertNotNull(schedule.getScheduleLocations()),
                () -> assertThat(schedule.getScheduleLocations().size()).isEqualTo(3),
                () -> schedule.getScheduleLocations().forEach(scheduleLocation -> {
                    assertAll(
                            () -> assertNotNull(scheduleLocation.getScheduleDetails()),
                            () -> assertThat(scheduleLocation.getScheduleDetails().size()).isEqualTo(3)
                    );
                })
        );
    }


    //일정 게시물 생성 메소드
    public Schedule createSchedule(Account account, int index, int locationCount, int detailCount, Scope scope) {
        Schedule schedule = Schedule.builder()
                .account(account)
                .title("schedule" + index)
                .scope(scope)
                .regDate(LocalDateTime.now())
                .viewCount(0)
                .build();
        Schedule savedSchedule = scheduleRepository.save(schedule);
        List<ScheduleLocation> scheduleLocations = createScheduleLocations(savedSchedule, locationCount, detailCount);
        savedSchedule.setScheduleLocations(scheduleLocations);

        return savedSchedule;
    }

    //일정의 세부 장소 아이템 생성 메소드
    private List<ScheduleLocation> createScheduleLocations(Schedule schedule, int locationCount, int detailCount) {
        List<ScheduleLocation> scheduleLocations = new ArrayList<>();
        for (int i = 0; i < locationCount; i++) {
            ScheduleLocation scheduleLocation = ScheduleLocation.builder()
                    .schedule(schedule)
                    .location("location" + i)
                    .latitude(33.0000000)
                    .longitude(120.0000000)
                    .build();
            ScheduleLocation savedScheduleLocation = scheduleLocationRepository.save(scheduleLocation);
            scheduleLocations.add(savedScheduleLocation);
        }
        AtomicInteger numForDateValid = new AtomicInteger();//세부 일정 간의 날짜 유효성을 맞춰주기 위해서 생성한 변수
        numForDateValid.set(detailCount);
        createScheduleDetails(scheduleLocations, numForDateValid, detailCount);
        return scheduleLocations;
    }

    //세부 장소의 세부 일정 아이템 생성 메소드
    private void createScheduleDetails(List<ScheduleLocation> scheduleLocations, AtomicInteger numForDateValid, int detailCount) {
        scheduleLocations.forEach(scheduleLocation -> {
            List<ScheduleDetail> scheduleDetails = new ArrayList<>();
            for (int i = numForDateValid.get() - detailCount; i < numForDateValid.get(); i++) {
                ScheduleDetail scheduleDetail = ScheduleDetail.builder()
                        .place("Place" + i + " in location")
                        .plan("Do something in place" + i)
                        .startDate(LocalDateTime.of(2020, 5, i + 1, 13, 0, 0))
                        .endDate(LocalDateTime.of(2020, 5, i + 2, 12, 0, 0))
                        .scheduleLocation(scheduleLocation)
                        .build();
                ScheduleDetail savedScheduleDetail = scheduleDetailRepository.save(scheduleDetail);
                scheduleDetails.add(savedScheduleDetail);
                //마지막 루프에서 날짜 유효성을 위해서 numForDateValid변수를 update해준다.
                if (i == numForDateValid.get() - 1) {
                    numForDateValid.set(detailCount + numForDateValid.get());
                    break;
                }
            }
            scheduleLocation.setScheduleDetails(scheduleDetails);
        });
    }


}