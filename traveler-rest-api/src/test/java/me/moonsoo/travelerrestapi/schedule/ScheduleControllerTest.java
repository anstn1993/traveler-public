package me.moonsoo.travelerrestapi.schedule;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerrestapi.BaseControllerTest;
import me.moonsoo.travelerrestapi.follow.Follow;
import me.moonsoo.travelerrestapi.follow.FollowRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Autowired
    private FollowRepository followRepository;

    @AfterEach
    public void tearDown() {
        followRepository.deleteAll();
        scheduleRepository.deleteAll();
        accountRepository.deleteAll();
    }


    @Test
    @DisplayName("일정 게시물 추가")
    public void createSchedule() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
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
                .andExpect(jsonPath("account.nickname").exists())
                .andExpect(jsonPath("account.profileImageUri").doesNotExist())
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
                                fieldWithPath("account.nickname").description("게시물 작성자의 닉네임"),
                                fieldWithPath("account.profileImageUri").description("게시물 작성자의 프로필 이미지 경로"),
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
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);

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
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);

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
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);

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
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(username, email, password, 0);
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
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Account otherAccount = createAccount(username, email, password, 1);
        //자신의 일정 게시물 15개, 다른 사용자의 일정 게시물 15 생성
        IntStream.range(0, 15).forEach(i -> {
            createSchedule(account, i, 3, 3, Scope.NONE);
            if (i % 2 == 0) {
                createSchedule(otherAccount, i + 15, 3, 3, Scope.ALL);
            } else {
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
                .andDo(document("get-schedules",
                        pagingLinks.and(
                                linkWithRel("profile").description("api 문서 링크"),
                                linkWithRel("create-schedule").description("일정 게시물 생성 링크(유효한 access token을 헤더에 포함시켜서 요청할 경우에만 활성화)")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("oauth2 access token"),
                                headerWithName(HttpHeaders.ACCEPT).description("응답 본문으로 받기를 원하는 컨텐츠 타입")
                        ),
                        requestParameters(
                                parameterWithName("page").optional().description("페이지 번호"),
                                parameterWithName("size").optional().description("한 페이지 당 게시물 수"),
                                parameterWithName("sort").optional().description("정렬 기준(id-게시물 id, regDate-등록 날짜, viewCount-조회수)"),
                                parameterWithName("filter").optional().description("검색어 필터(writer-작성자, title-제목, location-장소명)"),
                                parameterWithName("search").optional().description("검색어")
                        ),
                        responseHeaders.and(
                                headerWithName(HttpHeaders.CONTENT_LENGTH).description("응답 본문 데이터의 크기")
                        ),
                        responsePageFields.and(
                                fieldWithPath("_embedded.scheduleList[].id").description("일정 게시물 id"),
                                fieldWithPath("_embedded.scheduleList[].account.id").description("일정 게시물 작성자 id"),
                                fieldWithPath("_embedded.scheduleList[].account.nickname").description("일정 게시물 작성자 닉네임"),
                                fieldWithPath("_embedded.scheduleList[].account.profileImageUri").description("일정 게시물 작성자 프로필 이미지 경로"),
                                fieldWithPath("_embedded.scheduleList[].title").description("일정 게시물 제목"),
                                fieldWithPath("_embedded.scheduleList[].scope").description("일정 게시물 공개 범위(NONE인 게시물은 자신의 게시물이 아닌 경우에는 조회되지 않는다.)"),
                                fieldWithPath("_embedded.scheduleList[].regDate").description("일정 게시물 작성 시간"),
                                fieldWithPath("_embedded.scheduleList[].viewCount").description("일정 게시물 조회수"),
                                fieldWithPath("_embedded.scheduleList[]._links.self.href").description("해당 일정 게시물 조회 링크"),
                                fieldWithPath("_links.create-schedule.href").description("일정 게시물 생성 링크(유효한 access token을 헤더에 포함시켜서 요청할 경우에만 활성화)")
                        )
                ))
        ;
    }


    @Test
    @DisplayName("인증 상태에서 일정 게시물 목록 조회(자기 게시물 조회: Scope에 무관하게 모두 query)-검색어 조건: 작성자, 30개의 게시물, 한 페이지에 10개, 2페이지 가져오기")
    public void getSchedules_Filtered_By_Writer_With_Auth() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Account otherAccount = createAccount(username, email, password, 1);
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
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Account otherAccount = createAccount(username, email, password, 1);
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
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Account otherAccount = createAccount(username, email, password, 1);
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
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(username, email, password, 0);
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
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(username, email, password, 0);
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
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(username, email, password, 0);
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
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(username, email, password, 0);
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
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(username, email, password, 0);
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
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(username, email, password, 0);
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
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(username, email, password, 0);
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
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(username, email, password, 0);
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

    @DisplayName("인증 상태에서 자신의 일정 게시물 하나 조회(scope와 무관하게 모두 조회 가능)")
    @ParameterizedTest
    @MethodSource("scopeProvider")
    public void getMySchedule_With_Auth_Scope(Scope scope) throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);

        Schedule schedule = createSchedule(account, 0, 3, 3, scope);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/schedules/{scheduleId}", schedule.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("account.nickname").exists())
                .andExpect(jsonPath("account.profileImageUri").doesNotExist())
                .andExpect(jsonPath("title").exists())
                .andExpect(jsonPath("scope").exists())
                .andExpect(jsonPath("regDate").exists())
                .andExpect(jsonPath("viewCount").value(1))
                .andExpect(jsonPath("scheduleLocations").exists())
                .andExpect(jsonPath("scheduleLocations[0].scheduleDetails").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.get-schedules").exists())
                .andExpect(jsonPath("_links.update-schedule").exists())
                .andExpect(jsonPath("_links.delete-schedule").exists())
                .andDo(document("get-schedule", links(
                        linkWithRel("self").description("조회한 일정 게시물의 리소스 링크"),
                        linkWithRel("profile").description("api 문서 링크"),
                        linkWithRel("get-schedules").description("일정 게시물 목록 조회 링크"),
                        linkWithRel("update-schedule").description("일정 게시물을 수정할 수 있는 링크(인증상태에서 자신의 게시물을 조회한 경우에 활성화)"),
                        linkWithRel("delete-schedule").description("일정 게시물을 삭제할 수 있는 링크(인증상태에서 자신의 게시물을 조회한 경우에 활성화)")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("oauth2 access token"),
                                headerWithName(HttpHeaders.ACCEPT).description("응답 본문으로 받기를 원하는 컨텐츠 타입")
                        ),
                        pathParameters(
                                parameterWithName("scheduleId").description("일정 게시물의 id")
                        ),
                        responseHeaders.and(
                                headerWithName(HttpHeaders.CONTENT_LENGTH).description("응답 본문 데이터의 크기")
                        ),
                        responseFields(
                                fieldWithPath("id").description("일정 게시물의 id"),
                                fieldWithPath("account.id").description("게시물 작성자의 id"),
                                fieldWithPath("account.nickname").description("게시물 작성자의 닉네임"),
                                fieldWithPath("account.profileImageUri").description("게시물 작성자의 프로필 이미지 경로"),
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
                                fieldWithPath("_links.get-schedules.href").description("일일정 게시물 목록 조회 링크"),
                                fieldWithPath("_links.update-schedule.href").description("일정 게시물을 수정할 수 있는 링크(인증상태에서 자신의 게시물을 조회한 경우에 활성화)"),
                                fieldWithPath("_links.delete-schedule.href").description("일정 게시물을 삭제할 수 있는 링크(인증상태에서 자신의 게시물을 조회한 경우에 활성화)"),
                                fieldWithPath("_links.profile.href").description("api 문서 링크")
                        )
                ))
        ;
    }

    @DisplayName("인증 상태에서 타인의 일정 게시물 하나 조회(scope가 ALL)")
    @ParameterizedTest
    @MethodSource("scopeAndFollowingStatusProvider")
    public void getOthersSchedule_With_Auth_Scope_ALL(Scope scope, boolean following) throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Account otherAccount = createAccount(username, email, password, 1);
        Schedule schedule = createSchedule(otherAccount, 0, 3, 3, scope);

        if (scope.equals(Scope.FOLLOWER) && following) {
            //argument로 following이 true로 넘어오면 요청을 보내는 사용자가 일정 게시물의 작성자를 following하도록 처리
            createFollow(account, otherAccount);
        }

        ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.get("/api/schedules/{scheduleId}", schedule.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print());

        if (scope.equals(Scope.FOLLOWER) && !following) {//scope가 FOLLOWER인데 팔로잉하고 있지 않은 경우 forbidden
            resultActions.andExpect(status().isForbidden());
            return;
        } else if (scope.equals(Scope.NONE)) {//Scope.NONE인 경우 forbidden
            resultActions.andExpect(status().isForbidden());
            return;
        }

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("account.nickname").exists())
                .andExpect(jsonPath("account.profileImageUri").doesNotExist())
                .andExpect(jsonPath("title").exists())
                .andExpect(jsonPath("scope").exists())
                .andExpect(jsonPath("regDate").exists())
                .andExpect(jsonPath("viewCount").value(1))
                .andExpect(jsonPath("scheduleLocations").exists())
                .andExpect(jsonPath("scheduleLocations[0].scheduleDetails").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.get-schedules").exists())
                .andExpect(jsonPath("_links.update-schedule").doesNotHaveJsonPath())
                .andExpect(jsonPath("_links.delete-schedule").doesNotHaveJsonPath())
        ;
    }

    @DisplayName("인증하지 않은 상태에서 일정 게시물 하나 조회(scope가 ALL인 게시물만 조회 가능)")
    @ParameterizedTest
    @MethodSource("scopeProvider")
    public void getSchedule_Without_Auth_Scope(Scope scope) throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(username, email, password, 0);
        Schedule schedule = createSchedule(account, 0, 3, 3, scope);

        ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders.get("/api/schedules/{scheduleId}", schedule.getId())
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andDo(print());
        if (scope.equals(Scope.ALL)) {
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("id").exists())
                    .andExpect(jsonPath("account.id").exists())
                    .andExpect(jsonPath("account.nickname").exists())
                    .andExpect(jsonPath("account.profileImageUri").doesNotExist())
                    .andExpect(jsonPath("title").exists())
                    .andExpect(jsonPath("scope").exists())
                    .andExpect(jsonPath("regDate").exists())
                    .andExpect(jsonPath("viewCount").value(1))
                    .andExpect(jsonPath("scheduleLocations").exists())
                    .andExpect(jsonPath("scheduleLocations[0].scheduleDetails").exists())
                    .andExpect(jsonPath("_links.self").exists())
                    .andExpect(jsonPath("_links.profile").exists())
                    .andExpect(jsonPath("_links.get-schedules").exists())
                    .andExpect(jsonPath("_links.update-schedule").doesNotHaveJsonPath())
                    .andExpect(jsonPath("_links.delete-schedule").doesNotHaveJsonPath())
            ;
        } else {
            resultActions.andExpect(status().isForbidden());
        }
    }

    //매개변수 테스트 시 매개변수의 값을 전달해줄 메소드
    private static Stream<Arguments> scopeProvider() {
        return Stream.of(
                Arguments.of(Scope.ALL),
                Arguments.of(Scope.FOLLOWER),
                Arguments.of(Scope.NONE)
        );
    }

    //매개변수 테스트 시 매개변수의 값을 전달해줄 메소드
    private static Stream<Arguments> scopeAndFollowingStatusProvider() {
        return Stream.of(
                Arguments.of(Scope.ALL, false),
                Arguments.of(Scope.FOLLOWER, true),
                Arguments.of(Scope.FOLLOWER, false),
                Arguments.of(Scope.NONE, false)
        );
    }

    @Test
    @DisplayName("일정 게시물 하나 조회 실패-존재하지 않는 게시물(404 Not found)")
    public void getScheduleFail_Not_Found() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);

        mockMvc.perform(get("/api/schedules/{scheduleId}", 404)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
                .andExpect(status().isNotFound());
    }





    @Test
    @DisplayName("일정 게시물 추가 메소드 테스트")
    public void createScheduleMethod() {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(username, email, password, 0);
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

    @Test
    @DisplayName("일정 게시물 수정")
    public void updateSchedule() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);

        Schedule schedule = createSchedule(account, 0, 3, 3, Scope.ALL);//일정 게시물 생성

        ScheduleDto scheduleDto = createScheduleDto(0, Scope.FOLLOWER);//수정할 일정 게시물 DTO
        String title = "updated title";//수정할 게시물 제목
        scheduleDto.setTitle(title);
        for (ScheduleLocationDto scheduleLocationDto : scheduleDto.getScheduleLocationDtos()) {
            scheduleLocationDto.setLocation("updated location");
        }

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/schedules/{scheduleId}", schedule.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(scheduleDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account.id").exists())
                .andExpect(jsonPath("account.nickname").exists())
                .andExpect(jsonPath("account.profileImageUri").doesNotExist())
                .andExpect(jsonPath("title").value(title))
                .andExpect(jsonPath("scope").exists())
                .andExpect(jsonPath("regDate").exists())
                .andExpect(jsonPath("viewCount").exists())
                .andExpect(jsonPath("scheduleLocations").exists())
                .andExpect(jsonPath("scheduleLocations[0].scheduleDetails").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.get-schedules").exists())
                .andExpect(jsonPath("_links.delete-schedule").exists())
                .andDo(document("update-schedule",
                        links(
                                linkWithRel("self").description("수정된 일정 게시물의 리소스 링크"),
                                linkWithRel("get-schedules").description("일정 게시물 리스트를 조회할 수 있는 링크"),
                                linkWithRel("delete-schedule").description("수정된 일정 게시물을 삭제할 수 있는 링크"),
                                linkWithRel("profile").description("api 문서 링크")
                        ),
                        requestHeaders,
                        pathParameters(
                                parameterWithName("scheduleId").description("수정할 일정 게시물의 id")
                        ),
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
                                headerWithName(HttpHeaders.CONTENT_LENGTH).description("응답 본문 데이터의 크기")
                        ),
                        responseFields(
                                fieldWithPath("id").description("일정 게시물의 id"),
                                fieldWithPath("account.id").description("게시물 작성자의 id"),
                                fieldWithPath("account.nickname").description("게시물 작성자의 닉네임"),
                                fieldWithPath("account.profileImageUri").description("게시물 작성자의 프로필 이미지 경로"),
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
                                fieldWithPath("_links.delete-schedule.href").description("업로드된 일정 게시물을 삭제할 수 있는 링크"),
                                fieldWithPath("_links.profile.href").description("api 문서 링크")
                        )
                ))

        ;

    }

    @Test
    @DisplayName("일정 게시물 수정 실패-요청 본문이 없는 경우(400 Bad request)")
    public void updateScheduleFail_Bad_Request_Empty_Value() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);

        Schedule schedule = createSchedule(account, 0, 3, 3, Scope.ALL);//일정 게시물 생성
        ScheduleDto scheduleDto = ScheduleDto.builder().build();//빈 요청 본문

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/schedules/{scheduleId}", schedule.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(scheduleDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("일정 게시물 수정 실패-요청 본문에 허용되지 않은 값이 존재(400 Bad request)")
    public void updateScheduleFail_Bad_Request_Unknown_Property() throws Exception {
//Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);

        Schedule schedule = createSchedule(account, 0, 3, 3, Scope.ALL);//일정 게시물 생성

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/schedules/{scheduleId}", schedule.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(schedule)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("일정 게시물 수정 실패-요청 본문이 비즈니스 로직에 맞지 않는 경우(400 Bad request)")
    public void updateScheduleFail_Bad_Request_Wrong_Value() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);

        Schedule schedule = createSchedule(account, 0, 3, 3, Scope.ALL);//일정 게시물 생성
        ScheduleDto scheduleDtoWithWrongValue = createScheduleDtoWithWrongValue(0, Scope.FOLLOWER);//비즈니스 로직에 맞지 않는 일정 게시물 dto

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/schedules/{scheduleId}", schedule.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(scheduleDtoWithWrongValue)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("일정 게시물 수정 실패-oauth인증을 하지 않은 경우(401 Unauthorized)")
    public void updateScheduleFail_Unauthorized() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        account = createAccount(username, email, password, 0);

        Schedule schedule = createSchedule(account, 0, 3, 3, Scope.ALL);//일정 게시물 생성

        ScheduleDto scheduleDto = createScheduleDto(0, Scope.FOLLOWER);//수정할 일정 게시물 DTO
        String title = "updated title";//수정할 게시물 제목
        scheduleDto.setTitle(title);

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/schedules/{scheduleId}", schedule.getId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(scheduleDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
        ;
    }

    @Test
    @DisplayName("일정 게시물 수정 실패-타인의 일정을 수정하려고 하는 경우(403 Forbidden)")
    public void updateScheduleFail_Forbidden() throws Exception {
//Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);
        Account otherAccount = createAccount(username, email, password, 1);
        Schedule schedule = createSchedule(otherAccount, 0, 3, 3, Scope.ALL);//다른 사용자의 일정 게시물 생성

        ScheduleDto scheduleDto = createScheduleDto(0, Scope.FOLLOWER);//수정할 일정 게시물 DTO
        String title = "updated title";//수정할 게시물 제목
        scheduleDto.setTitle(title);

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/schedules/{scheduleId}", schedule.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(scheduleDto)))
                .andDo(print())
                .andExpect(status().isForbidden())
        ;
    }

    @Test
    @DisplayName("일정 게시물 수정 실패-존재하지 않는 일정을 수정하려고 하는 경우(404 Not found)")
    public void updateScheduleFail_Not_Found() throws Exception {
//Given
        String username = "anstn1993";
        String email = "anstn1993@email.com";
        String password = "1111";
        String accessToken = getAuthToken(username, email, password, 0);

        Schedule schedule = createSchedule(account, 0, 3, 3, Scope.ALL);//일정 게시물 생성

        ScheduleDto scheduleDto = createScheduleDto(0, Scope.FOLLOWER);//수정할 일정 게시물 DTO
        String title = "updated title";//수정할 게시물 제목
        scheduleDto.setTitle(title);

        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/schedules/{scheduleId}", 404)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(scheduleDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("일정 게시물 삭제")
    public void deleteSchedule() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);

        Schedule schedule = createSchedule(account, 0, 3, 3, Scope.ALL);

        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/schedules/{scheduleId}", schedule.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document("delete-schedule",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("oauth2 access token")
                        ),
                        pathParameters(
                                parameterWithName("scheduleId").description("삭제할 일정 게시물 id")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("일정 게시물 삭제 실패-oauth 인증을 하지 않은 경우(401 Unauthorized)")
    public void deleteScheduleFail_Unauthorized() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        account = createAccount(username, email, password, 0);

        Schedule schedule = createSchedule(account, 0, 3, 3, Scope.ALL);

        mockMvc.perform(delete("/api/schedules/{scheduleId}", schedule.getId()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("일정 게시물 삭제 실패-타인의 일정을 삭제하려고 하는 경우(403 Forbidden)")
    public void deleteScheduleFail_Forbidden() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);
        Account otherAccount = createAccount(username, email, password, 1);
        Schedule schedule = createSchedule(otherAccount, 0, 3, 3, Scope.ALL);//다른 사용자가 만든 일정 게시물

        mockMvc.perform(delete("/api/schedules/{scheduleId}", schedule.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("일정 게시물 삭제 실패-존재하지 않는 일정을 삭제하려고 하는 경우(404 Not found)")
    public void deleteScheduleFail_Not_Found() throws Exception {
        //Given
        String username = "anstn1993";
        String email = "user@email.com";
        String password = "user";
        String accessToken = getAuthToken(username, email, password, 0);

        mockMvc.perform(delete("/api/schedules/{scheduleId}", 404)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNotFound());
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
        Set<ScheduleLocation> scheduleLocations = createScheduleLocations(savedSchedule, locationCount, detailCount);
        savedSchedule.setScheduleLocations(scheduleLocations);
        return savedSchedule;
    }

    //일정의 세부 장소 아이템 생성 메소드
    private Set<ScheduleLocation> createScheduleLocations(Schedule schedule, int locationCount, int detailCount) {
        Set<ScheduleLocation> scheduleLocations = new LinkedHashSet<>();
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
    private void createScheduleDetails(Set<ScheduleLocation> scheduleLocations, AtomicInteger numForDateValid, int detailCount) {
        scheduleLocations.forEach(scheduleLocation -> {
            Set<ScheduleDetail> scheduleDetails = new LinkedHashSet<>();
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

    //request 요청 본문으로 binding될 dto객체 생성 메소드
    private ScheduleDto createScheduleDto(int index, Scope scope) {

        AtomicInteger day = new AtomicInteger(1);//schedule detail의 시작 날짜와 종료 날짜의 day에 사용될 변수
        LinkedHashSet<ScheduleLocationDto> scheduleLocationDtos = new LinkedHashSet<>();//일정 장소 dto set
        IntStream.range(0, 3).forEach(i -> {
            LinkedHashSet<ScheduleDetailDto> scheduleDetailDtos = new LinkedHashSet<>();//상세 일정 dto set
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
        Set<ScheduleLocation> scheduleLocations = new LinkedHashSet<>();//일정 장소 dto set
        IntStream.range(0, 3).forEach(i -> {
            Set<ScheduleDetail> scheduleDetails = new LinkedHashSet<>();//상세 일정 dto set
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

        LinkedHashSet<ScheduleDetailDto> scheduleDetailDtos = new LinkedHashSet<>();//상세 일정 dto set
        IntStream.range(0, 3).forEach(i -> {
            ScheduleDetailDto scheduleDetailDto = ScheduleDetailDto.builder()
                    .place("Place" + i + " in location")
                    .plan("Do something in place" + i)
                    .startDate(LocalDateTime.of(2020, 5, i + 2, 12, 0, 0))
                    .endDate(LocalDateTime.of(2020, 5, i + 1, 12, 0, 0))
                    .build();
            scheduleDetailDtos.add(scheduleDetailDto);
        });

        LinkedHashSet<ScheduleLocationDto> scheduleLocationDtos = new LinkedHashSet<>();//일정 장소 dto set
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

    private Follow createFollow(Account followingAccount, Account followedAccount) {
        Follow follow = Follow.builder()
                .followingAccount(followingAccount)
                .followedAccount(followedAccount)
                .build();

        return followRepository.save(follow);
    }


}