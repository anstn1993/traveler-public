package me.moonsoo.travelerrestapi.accompany;

import lombok.extern.slf4j.Slf4j;
import me.moonsoo.commonmodule.account.AccountAdapter;
import me.moonsoo.travelerrestapi.errors.ErrorsModel;
import me.moonsoo.travelerrestapi.properties.AppProperties;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

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
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        Accompany accompany = modelMapper.map(accompanyDto, Accompany.class);
        accompany.setAccount(accountAdapter.getAccount());
        accompany.setRegDate(LocalDateTime.now());//게시물 생성 시간 등록
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

    @GetMapping
    public ResponseEntity getAccompanies(Pageable pageable,
                                         PagedResourcesAssembler<Accompany> assembler,
                                         @RequestParam Map<String, String> params,
                                         @AuthenticationPrincipal AccountAdapter accountAdapter) {
        String filter = params.get("filter");
        String search = params.get("search");
        Page<Accompany> accompanies = null;
        if (filter == null || filter.isBlank() || search == null || search.isBlank()) {
            accompanies = accompanyRepository.findAll(pageable);
        } else if (filter.equals("writer")) {
            accompanies = accompanyRepository.findAllByAccount_NicknameContains(search, pageable);
        } else if (filter.equals("title")) {
            accompanies = accompanyRepository.findAllByTitleContains(search, pageable);
        } else if(filter.equals("article")) {
            accompanies = accompanyRepository.findAllByArticleContains(search, pageable);
        } else {//filter.equals("location")
            accompanies = accompanyRepository.findAllByLocationContains(search, pageable);
        }

        PagedModel<AccompanyModel> accompanyModels =
                assembler.toModel(
                        accompanies,
                        a -> new AccompanyModel(a),
                        //page링크에 filter, search 같은 request param을 함께 붙이기 위해서 필요한 링크
                        linkTo(methodOn(AccompanyController.class).getAccompanies(pageable, assembler, params, accountAdapter)).withSelfRel()
                );//a: accompany
        accompanyModels.add(new Link(appProperties.getBaseUrl() + appProperties.getProfileUri()).withRel("profile"));
        if (accountAdapter != null) {
            //인증된 상태에서 접근할 때는 게시물 생성 링크까지 제공
            accompanyModels.add(linkTo(AccompanyController.class).withRel("create-accompany"));
        }
        return ResponseEntity.ok(accompanyModels);
    }
}
