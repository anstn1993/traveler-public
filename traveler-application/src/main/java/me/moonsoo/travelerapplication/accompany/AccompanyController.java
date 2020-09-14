package me.moonsoo.travelerapplication.accompany;

import me.moonsoo.travelerapplication.accompany.comment.AccompanyCommentDeserializer;
import me.moonsoo.travelerapplication.accompany.comment.AccompanyCommentModel;
import me.moonsoo.travelerapplication.account.SessionAccount;
import me.moonsoo.travelerapplication.deserialize.CustomPagedModel;
import me.moonsoo.travelerapplication.properties.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/accompanies")
public class AccompanyController {

    @Autowired
    private AccompanyValidator validator;

    @Autowired
    private OAuth2RestTemplate oAuth2RestTemplate;

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private AccompanyDeserializer accompanyDeserializer;

    @Autowired
    private AccompanyCommentDeserializer accompanyCommentDeserializer;

    //동행 구하기 게시판 페이지 요청 핸들러
    @GetMapping
    public String getAccompanyBoardPage(Pageable pageable,
                                        @RequestParam Map<String, String> params,
                                        Model model) throws InvocationTargetException, NoSuchMethodException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        String url = null;
        String filter = params.get("filter");
        String search = params.get("search");
        if(filter != null && !filter.isBlank() && search != null && !search.isBlank()) {
            url = appProperties.getRestApiUrl() + "/accompanies?page=" + pageable.getPageNumber()
                    + "&size=" + pageable.getPageSize() + "&sort=id,DESC&filter=" + filter + "&search=" + search;
            model.addAttribute("search", search);
            model.addAttribute("filter", filter);
        }
        else {
            url = appProperties.getRestApiUrl() + "/accompanies?page=" + pageable.getPageNumber()
                    + "&size=" + pageable.getPageSize() + "&sort=id,DESC";
        }
        ResponseEntity<Object> response = oAuth2RestTemplate.getForEntity(url, Object.class);
        AccompanyDeserializer resourceDeserializer = new AccompanyDeserializer();
        CustomPagedModel<AccompanyModel> accompanyList = resourceDeserializer.deseriazizePagedModel(response.getBody(), "accompanyList");
        model.addAttribute("accompanyList", accompanyList);
        return "/accompany/accompany-board";
    }

    //동행 게시물 업로드 페이지 요청 핸들러
    @GetMapping("/upload")
    public String getUploadAccompanyPage(@SessionAttribute(required = false)SessionAccount account) {
        //로그인 상태가 아니라면 login 페이지로 리다이렉트
        if(account == null) {
            return "redirect:/login";
        }
        return "/accompany/upload-accompany";
    }

    //동행 게시물 생성 요청 핸들러
    @PostMapping
    public ResponseEntity createAccompany(@SessionAttribute(required = false) SessionAccount account,
                                          @RequestBody @Valid AccompanyDto accompanyDto, Errors errors) throws OAuth2AccessDeniedException {
        if(account == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        //데이터 유효성 검사
        validator.validate(accompanyDto, errors);
        if(errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaTypes.HAL_JSON));
        HttpEntity<AccompanyDto> request = new HttpEntity<>(accompanyDto, headers);
        ResponseEntity<Object> responseEntity = oAuth2RestTemplate.postForEntity(appProperties.getRestApiUrl() + "/accompanies", request, Object.class);

        //요청 파라미터의 유효성 체크
        return responseEntity;
    }

    //동행 게시물 페이지 요청 핸들러
    @GetMapping("/{accompanyId}")
    public String getAccompanyPage(@PathVariable Integer accompanyId,
                                   Model model) throws InvocationTargetException, NoSuchMethodException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        //동행 게시물 데이터 fetch
        ResponseEntity<Object> accompanyResponse =
                oAuth2RestTemplate.getForEntity(appProperties.getRestApiUrl() + "/accompanies/" + accompanyId, Object.class);
        //동행 게시물 댓글 목록 데이터 fetch
        ResponseEntity<Object> commentResponse =
                oAuth2RestTemplate.getForEntity(appProperties.getRestApiUrl() + "/accompanies/" + accompanyId + "/comments?page=0&size=10&sort=id,ASC", Object.class);
        AccompanyModel accompanyModel = accompanyDeserializer.deserializeModel(accompanyResponse.getBody());
        CustomPagedModel<AccompanyCommentModel> accompanyCommentList = accompanyCommentDeserializer.deseriazizePagedModel(commentResponse.getBody(), "accompanyCommentList");
        model.addAttribute("accompany", accompanyModel);
        model.addAttribute("commentList", accompanyCommentList);
        return "/accompany/accompany";
    }
}
