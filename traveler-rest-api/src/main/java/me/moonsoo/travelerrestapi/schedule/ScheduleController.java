package me.moonsoo.travelerrestapi.schedule;


import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.CurrentAccount;
import me.moonsoo.travelerrestapi.errors.ErrorsModel;
import me.moonsoo.travelerrestapi.follow.Follow;
import me.moonsoo.travelerrestapi.follow.FollowService;
import me.moonsoo.travelerrestapi.properties.AppProperties;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
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

import javax.validation.Valid;
import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    @Autowired
    ScheduleValidator scheduleValidator;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    ScheduleService scheduleService;

    @Autowired
    FollowService followService;

    @Autowired
    AppProperties appProperties;

    //일정 게시물 생성 핸들러
    @PostMapping
    public ResponseEntity createSchedule(@RequestBody @Valid ScheduleDto scheduleDto,
                                         Errors errors,
                                         @CurrentAccount Account account) {
        //요청 본문이 없거나 허용되지 않은 값이 포함된 경우
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        //비즈니스 로직에 맞지 않은 값인 경우
        scheduleValidator.validate(scheduleDto, errors);
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        Schedule schedule = parseDtoToEntity(scheduleDto);
        Schedule savedSchedule = scheduleService.save(account, schedule);//db에 일정 저장

        //Hateoas적용
        ScheduleModel scheduleModel = new ScheduleModel(savedSchedule);
        WebMvcLinkBuilder linkBuilder = linkTo(ScheduleController.class);
        URI uri = linkBuilder.slash(savedSchedule.getId()).toUri();//헤더에 리소스 location 추가
        Link getSchedulesLink = linkBuilder.withRel("get-schedules");//게시물 조회 링크
        Link updateScheduleLink = linkBuilder.slash(savedSchedule.getId()).withRel("update-schedule");//게시물 수정 링크
        Link deleteScheduleLink = linkBuilder.slash(savedSchedule.getId()).withRel("delete-schedule");//게시물 삭제 링크
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getGetSchedulesAnchor()).withRel("profile");//profile 링크
        scheduleModel.add(getSchedulesLink, updateScheduleLink, deleteScheduleLink, profileLink);//profile링크
        return ResponseEntity.created(uri).body(scheduleModel);
    }

    //일정 게시물 목록 조회 핸들러
    @GetMapping
    public ResponseEntity getSchedules(Pageable pageable,
                                       PagedResourcesAssembler<ScheduleWithoutLocations> assembler,
                                       @RequestParam Map<String, String> params,
                                       @CurrentAccount Account account) {

        Page<ScheduleWithoutLocations> schedules = scheduleService.findAll(pageable, account, params);//조건에 맞는 일정 게시물 query
        PagedModel<ScheduleWithoutLocationsModel> schedulePagedModel =
                assembler.toModel(
                        schedules,
                        s -> new ScheduleWithoutLocationsModel(s),
                        //page링크에 filter, search 같은 request param을 함께 붙이기 위해서 필요한 링크
                        linkTo(methodOn(ScheduleController.class).getSchedules(pageable, assembler, params, account)).withSelfRel());

        //Object mapper가 ScheduleWithoutLocationsSerializer를 사용해서 serializing을 하게 하기 위해서 Paged Model을 ScheduleWithoutLocationsPagedModel로 교체해준다.
        ScheduleWithoutLocationsPagedModel scheduleWithoutLocationsPagedModel =
                new ScheduleWithoutLocationsPagedModel(schedulePagedModel.getContent(),
                        schedulePagedModel.getMetadata(),
                        schedulePagedModel.getLinks());

        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getGetSchedulesAnchor()).withRel("profile");//profile 링크
        if (account != null) {//인증상태인 경우 일정 게시물 생성 링크 추가
            Link createScheduleLink = linkTo(ScheduleController.class).withRel("create-schedule");
            scheduleWithoutLocationsPagedModel.add(createScheduleLink);
        }
        scheduleWithoutLocationsPagedModel.add(profileLink);
        return ResponseEntity.ok(scheduleWithoutLocationsPagedModel);
    }



    //일정 게시물 조회 핸들러
    @GetMapping("/{scheduleId}")
    public ResponseEntity getSchedules(@PathVariable("scheduleId") Schedule schedule,
                                       @CurrentAccount Account account) {

        if (schedule == null) {//존재하지 않는 리소스인 경우 404
            return ResponseEntity.notFound().build();
        }

        if (account == null && !schedule.getScope().equals(Scope.ALL)) {//인증하지 않은 상태 && scope가 ALL이 아닌 경우
            Errors errors = new DirectFieldBindingResult(schedule, "scope");
            errors.reject("forbidden", "You can not access to this resource.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorsModel(errors));

        }

        if (account != null && !schedule.getAccount().equals(account)) {//인증 상태 && 타인의 일정 게시물
            switch (schedule.getScope()) {
                case ALL:
                    break;
                case FOLLOWER:
                    Optional<Follow> followOpt = followService.getFollow(account, schedule.getAccount());
                    //게시물 작성자를 팔로잉하고 있지 않은 경우
                    if (followOpt.isEmpty()) {
                        Errors errors = new DirectFieldBindingResult(schedule, "scope");
                        errors.reject("forbidden", "You can not access to this resource.");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorsModel(errors));
                    }
                    //팔로잉하고 있는 경우
                    break;
                case NONE:
                    Errors errors = new DirectFieldBindingResult(schedule, "scope");
                    errors.reject("forbidden", "You can not access to this resource.");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorsModel(errors));
            }
        }

        //조회수 +1
        schedule.setViewCount(schedule.getViewCount() + 1);
        Schedule updatedSchedule = scheduleService.updateViewCount(schedule);

        //Hateoas적용
        ScheduleModel scheduleModel = new ScheduleModel(updatedSchedule);
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getGetScheduleAnchor()).withRel("profile");//profile 링크
        Link getSchedulesLink = linkTo(ScheduleController.class).withRel("get-schedules");//일정 게시물 목록 조회 링크
        scheduleModel.add(profileLink, getSchedulesLink);

        if (account != null && updatedSchedule.getAccount().equals(account)) {//인증 상태 && 자신의 일정 게시물
            //게시물 수정, 삭제 링크 제공
            WebMvcLinkBuilder linkBuilder = linkTo(Schedule.class).slash(updatedSchedule.getId());
            Link updateLink = linkBuilder.withRel("update-schedule");
            Link deleteLink = linkBuilder.withRel("delete-schedule");
            scheduleModel.add(updateLink, deleteLink);
            return ResponseEntity.ok(scheduleModel);
        }

        return ResponseEntity.ok(scheduleModel);
    }

    //일정 게시물 수정 핸들러
    @PutMapping("/{scheduleId}")
    public ResponseEntity updateSchedule(@PathVariable("scheduleId") Schedule schedule,
                                         @RequestBody @Valid ScheduleDto scheduleDto,
                                         Errors errors,
                                         @CurrentAccount Account account) {

        //존재하지 않는 리소스인 경우
        if(schedule == null) {
            return ResponseEntity.notFound().build();
        }

        //다른 사용자의 리소스를 수정하려고 하는 경우
        if(!schedule.getAccount().equals(account)) {
            errors.reject("forbidden", "You can not update other user's contents.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorsModel(errors));
        }

        //요청 본문이 없거나 허용되지 않은 값이 넘어온 경우
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        //요청 본문의 값이 비즈니스 로직에 맞지 않는 경우
        scheduleValidator.validate(scheduleDto, errors);
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        //기존의 schedule의 하위 scheduleLocation 제거
        scheduleService.deleteScheduleLocations(schedule);

        //기존 리소스 엔티티로 요청 본문의 값 전달
        Schedule updatedSchedule = parseDtoToEntity(schedule, scheduleDto);
        Schedule savedSchedule = scheduleService.update(updatedSchedule);//수정된 일정 save

        //Hateoas적용
        ScheduleModel scheduleModel = new ScheduleModel(savedSchedule);
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getUpdateScheduleAnchor()).withRel("profile");//profile 링크
        WebMvcLinkBuilder linkBuilder = linkTo(ScheduleController.class);
        Link getSchedulesLink = linkBuilder.withRel("get-schedules");//일정 게시물 목록 조회 링크
        Link deleteLink = linkBuilder.withRel("delete-schedule");//일정 게시물 삭제 링크
        scheduleModel.add(profileLink, getSchedulesLink, deleteLink);
        return ResponseEntity.ok(scheduleModel);
    }


    //일정 게시물 삭제 핸들러
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity deleteSchedule(@PathVariable("scheduleId") Schedule schedule,
                                         @CurrentAccount Account account) {

        if (schedule == null) {//존재하지 않는 게시물
            return ResponseEntity.notFound().build();
        }

        if (!schedule.getAccount().equals(account)) {//자신의 게시물이 아닌 경우
            Errors errors = new DirectFieldBindingResult(account, "account");
            errors.reject("forbidden", "You can not delete other user's contents.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorsModel(errors));
        }

        scheduleService.delete(schedule);//리소스 삭제
        return ResponseEntity.noContent().build();
    }

    //DTO객체를 엔티티 객체로 변환해주는 메소드
    private Schedule parseDtoToEntity(ScheduleDto scheduleDto) {
        Schedule schedule = modelMapper.map(scheduleDto, Schedule.class);
        LinkedHashSet<ScheduleLocation> scheduleLocations = modelMapper.map(scheduleDto.getScheduleLocationDtos(), new TypeToken<LinkedHashSet<ScheduleLocation>>(){}.getType());
        schedule.setScheduleLocations(scheduleLocations);
        List<ScheduleLocation> scheduleLocationList = List.copyOf(scheduleLocations);
        AtomicInteger index = new AtomicInteger();
        for (ScheduleLocationDto scheduleLocationDto : scheduleDto.getScheduleLocationDtos()) {
            LinkedHashSet<ScheduleDetail> scheduleDetails =
                    modelMapper.map(scheduleLocationDto.getScheduleDetailDtos(), new TypeToken<LinkedHashSet<ScheduleDetail>>(){}.getType());
            scheduleLocationList.get(index.get()).setScheduleDetails(scheduleDetails);
            index.getAndIncrement();
        }
        return schedule;
    }

    //DTO객체를 persistence 엔티티 객체로 변환해주는 메소드
    private Schedule parseDtoToEntity(Schedule schedule, ScheduleDto scheduleDto) {
        modelMapper.map(scheduleDto, schedule);
        LinkedHashSet<ScheduleLocation> scheduleLocations = modelMapper.map(scheduleDto.getScheduleLocationDtos(), new TypeToken<LinkedHashSet<ScheduleLocation>>(){}.getType());
        schedule.setScheduleLocations(scheduleLocations);
        List<ScheduleLocation> scheduleLocationList = List.copyOf(scheduleLocations);
        AtomicInteger index = new AtomicInteger();
        for (ScheduleLocationDto scheduleLocationDto : scheduleDto.getScheduleLocationDtos()) {
            LinkedHashSet<ScheduleDetail> scheduleDetails =
                    modelMapper.map(scheduleLocationDto.getScheduleDetailDtos(), new TypeToken<LinkedHashSet<ScheduleDetail>>(){}.getType());
            scheduleLocationList.get(index.get()).setScheduleDetails(scheduleDetails);
            index.getAndIncrement();
        }
        return schedule;
    }
}
