package me.moonsoo.travelerrestapi.accompany;

import lombok.extern.slf4j.Slf4j;
import me.moonsoo.travelerrestapi.account.AccountAdapter;
import me.moonsoo.travelerrestapi.errors.ErrorsModel;
import me.moonsoo.travelerrestapi.properties.AppProperties;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.net.URI;
import java.time.LocalDateTime;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping(value = "/api/accompanies")
@Slf4j
public class AccompanyController {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AccompanyRepository accompanyRepository;

    @Autowired
    AccompanyValidator accompanyValidator;

    @Autowired
    AppProperties appProperties;

    @PostMapping
    public ResponseEntity createAccompany(@RequestBody @Valid AccompanyDto accompanyDto,
                                          Errors errors,
                                          @AuthenticationPrincipal AccountAdapter accountAdapter) {
        //요청 본문으로 넘어온 값 자체가 유효하지 않은 경우
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        //비즈니스 로직상 값들이 유효하지 않은 경우
        accompanyValidator.validate(accompanyDto, errors);
        if(errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        Accompany accompany = modelMapper.map(accompanyDto, Accompany.class);
        accompany.setAccount(accountAdapter.getAccount());
        accompany.setRegDate(LocalDateTime.now());
        Accompany savedAccompany = accompanyRepository.save(accompany);
        URI uri = linkTo(AccompanyController.class).slash(savedAccompany.getId()).toUri();//헤더에 리소스 location 추가
        //hateoas적용을 위해서 link정보들을 추가할 수 있는 Model 객체 생성
        AccompanyModel accompanyModel = new AccompanyModel(savedAccompany);
        Link getEventsLink = linkTo(AccompanyController.class).withRel("get-accompanies");//게시물 조회 링크
        Link updateEventLink = linkTo(AccompanyController.class).slash(savedAccompany.getId()).withRel("update-accompany");//게시물 수정 링크
        Link deleteEventLink = linkTo(AccompanyController.class).slash(savedAccompany.getId()).withRel("delete-accompany");//게시물 삭제 링크
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri()).withRel("profile");//profile 링크
        accompanyModel.add(getEventsLink, updateEventLink, deleteEventLink, profileLink);//링크 추가
        return ResponseEntity.created(uri).body(accompanyModel);
    }
}
