package me.moonsoo.travelerrestapi.schedule;


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
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Map;

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
    AppProperties appProperties;

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

    @GetMapping
    public ResponseEntity getSchedules(Pageable pageable,
                                       PagedResourcesAssembler<ScheduleWithoutLocations> assembler,
                                       @RequestParam Map<String, String> params,
                                       @CurrentAccount Account account) {

        Page<ScheduleWithoutLocations> schedules = scheduleService.findAll(pageable, account, params);//조건에 맞는 일정 게시물 query
        PagedModel<ScheduleWithoutLocationsModel> scheduleModels =
                assembler.toModel(
                        schedules,
                        s -> new ScheduleWithoutLocationsModel(s),
                        //page링크에 filter, search 같은 request param을 함께 붙이기 위해서 필요한 링크
                        linkTo(methodOn(ScheduleController.class).getSchedules(pageable, assembler, params, account)).withSelfRel());
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getCreateScheduleAnchor()).withRel("profile");//profile 링크
        if(account != null) {//인증상태인 경우 일정 게시물 생성 링크 추가
            Link createScheduleLink = linkTo(ScheduleController.class).withRel("create-schedule");
            scheduleModels.add(createScheduleLink);
        }
        scheduleModels.add(profileLink);
        return ResponseEntity.ok(scheduleModels);
    }

    //DTO객체를 엔티티 객체로 변환해주는 메소드
    private Schedule parseDtoToEntity(ScheduleDto scheduleDto) {
        Schedule schedule = modelMapper.map(scheduleDto, Schedule.class);
        scheduleDto.getScheduleLocationDtos().forEach(scheduleLocationDto -> {
            ScheduleLocation scheduleLocation = modelMapper.map(scheduleLocationDto, ScheduleLocation.class);
            scheduleLocationDto.getScheduleDetailDtos().forEach(scheduleDetailDto -> {
                ScheduleDetail scheduleDetail = modelMapper.map(scheduleDetailDto, ScheduleDetail.class);
                scheduleDetail.setScheduleLocation(scheduleLocation);
                scheduleLocation.getScheduleDetails().add(scheduleDetail);
            });
            scheduleLocation.setSchedule(schedule);
            schedule.getScheduleLocations().add(scheduleLocation);
        });
        return schedule;
    }
}
