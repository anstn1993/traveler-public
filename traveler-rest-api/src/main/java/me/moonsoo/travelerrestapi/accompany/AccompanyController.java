package me.moonsoo.travelerrestapi.accompany;

import lombok.extern.slf4j.Slf4j;
import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.CurrentAccount;
import me.moonsoo.travelerrestapi.errors.ErrorsModel;
import me.moonsoo.travelerrestapi.properties.AppProperties;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.logging.ErrorManager;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/accompanies")
@Slf4j
public class AccompanyController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AccompanyService accompanyService;

    @Autowired
    private AccompanyValidator accompanyValidator;

    @Autowired
    private AppProperties appProperties;

    //게시물 생성 핸들러
    @PostMapping
    public ResponseEntity createAccompany(@RequestBody @Valid AccompanyDto accompanyDto,
                                          Errors errors,
                                          @CurrentAccount Account account) {
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
        Accompany savedAccompany = accompanyService.save(accompany, account);//동행 게시물 db에 저장
        AccompanyModel accompanyModel = new AccompanyModel(savedAccompany);

        //Hateoas 적용
        WebMvcLinkBuilder linkBuilder = linkTo(AccompanyController.class);
        URI uri = linkBuilder.slash(savedAccompany.getId()).toUri();//헤더에 리소스 location 추가
        //hateoas적용을 위해서 link정보들을 추가할 수 있는 Model 객체 생성
        Link getAccompanysLink = linkBuilder.withRel("get-accompanies");//게시물 조회 링크
        Link updateAccompanyLink = linkBuilder.slash(savedAccompany.getId()).withRel("update-accompany");//게시물 수정 링크
        Link deleteAccompanyLink = linkBuilder.slash(savedAccompany.getId()).withRel("delete-accompany");//게시물 삭제 링크
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getCreateAccompanyAnchor()).withRel("profile");//profile 링크
        accompanyModel.add(getAccompanysLink, updateAccompanyLink, deleteAccompanyLink, profileLink);//링크 추가
        return ResponseEntity.created(uri).body(accompanyModel);
    }

    //게시물 목록 조회 핸들러
    @GetMapping
    public ResponseEntity getAccompanies(Pageable pageable,
                                         PagedResourcesAssembler<Accompany> assembler,
                                         @RequestParam Map<String, String> params,
                                         @CurrentAccount Account account) {
        String filter = params.get("filter");//검색 필터링 카테고리
        String search = params.get("search");//검색어
        Page<Accompany> accompanies = accompanyService.findAccompanies(pageable, filter, search);//동행 게시물 목록 조회

        //Hateoas적용
        PagedModel<AccompanyModel> accompanyModels =
                assembler.toModel(
                        accompanies,
                        a -> new AccompanyModel(a, linkTo(AccompanyController.class).slash(a.getId()).slash("comments").withRel("get-accompany-comments")),
                        //page링크에 filter, search 같은 request param을 함께 붙이기 위해서 필요한 링크
                        linkTo(methodOn(AccompanyController.class).getAccompanies(pageable, assembler, params, account)).withSelfRel()
                );//a: accompany
        accompanyModels.add(new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getGetAccompaniesAnchor()).withRel("profile"));
        if (account != null) {
            //인증된 상태에서 접근할 때는 게시물 생성 링크까지 제공
            accompanyModels.add(linkTo(AccompanyController.class).withRel("create-accompany"));
        }
        return ResponseEntity.ok(accompanyModels);
    }

    //게시물 조회 핸들러
    @GetMapping("/{id}")
    public ResponseEntity getAccompany(@PathVariable("id") Accompany accompany, @CurrentAccount Account account) {
        //요청한 리소스가 존재하지 않는 경우
        if (accompany == null) {
            return ResponseEntity.notFound().build();
        }
        Accompany updatedAccompany = accompanyService.updateViewCount(accompany);//조회수 1증가 처리
        AccompanyModel accompanyModel = new AccompanyModel(updatedAccompany);
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getGetAccompanyAnchor()).withRel("profile");
        Link getCommentsLink = linkTo(AccompanyController.class).slash(accompany.getId()).slash("comments").withRel("get-accompany-comments");//게시물의 댓글 목록 조회 링크
        Link getAccompaniesLink = linkTo(AccompanyController.class).withRel("get-accompanies");
        //인증 && 자신의 게시물인 경우 update, delete link 제공
        if (account != null && updatedAccompany.getAccount().equals(account)) {
            WebMvcLinkBuilder linkBuilder = linkTo(AccompanyController.class).slash(updatedAccompany.getId());
            Link updateLink = linkBuilder.withRel("update-accompany");
            Link deleteLink = linkBuilder.withRel("delete-accompany");
            accompanyModel.add(updateLink, deleteLink);
        }
        accompanyModel.add(profileLink, getCommentsLink, getAccompaniesLink);
        return ResponseEntity.ok(accompanyModel);
    }

    //게시물 수정 핸들러
    @PutMapping("/{id}")
    public ResponseEntity updateAccompany(@PathVariable("id") Accompany accompany,
                                          @RequestBody @Valid AccompanyDto accompanyDto,
                                          Errors errors,
                                          @CurrentAccount Account account) {
        //요청한 리소스가 존재하지 않는 경우
        if(accompany == null) {
            return ResponseEntity.notFound().build();
        }

        //다른 사용자의 게시물을 수정하려고 하는 경우
        if(!accompany.getAccount().equals(account)) {
            errors.reject("forbidden", "You can not update other user's contents.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorsModel(errors));
        }

        //요청 본문의 값들이 유효하지 않은 경우
        if(errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        //요청 본문의 값들이 비즈니스 로직에 부합하지 않는 경우
        accompanyValidator.validate(accompanyDto, errors);
        if(errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        modelMapper.map(accompanyDto, accompany);//기존 리소스 객체에 요청 본문으로 넘어온 데이터를 write

        Accompany updatedAccompany = accompanyService.save(accompany);
        AccompanyModel accompanyModel = new AccompanyModel(updatedAccompany);
        Link getAccompaniesLink = linkTo(AccompanyController.class).withRel("get-accompanies");//게시물 조회 링크
        Link deleteAccompanyLink = linkTo(AccompanyController.class).slash(updatedAccompany.getId()).withRel("delete-accompany");//게시물 삭제 링크
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getUpdateAccompanyAnchor()).withRel("profile");
        accompanyModel.add(getAccompaniesLink, deleteAccompanyLink, profileLink);
        return ResponseEntity.ok(accompanyModel);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteAccompany(@PathVariable("id") Accompany accompany, @CurrentAccount Account account) {
        //요청한 리소스가 존재하지 않는 경우
        if(accompany == null) {
            return ResponseEntity.notFound().build();
        }

        //요청한 리소스가 사용자의 리소스가 아닌 경우
        if(!accompany.getAccount().equals(account)) {
            Errors errors = new DirectFieldBindingResult(account, "account");
            errors.reject("forbidden", "You can not delete other user's contents.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        accompanyService.delete(accompany);//리소스 삭제
        return ResponseEntity.noContent().build();
    }
}
