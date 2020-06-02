package me.moonsoo.travelerrestapi.schedule;

import me.moonsoo.commonmodule.account.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.validation.Errors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ScheduleValidatorTest {

    @Test
    @DisplayName("세부 일정의 시작 날짜, 종료 날짜들의 유효성 통과 테스트")
    public void testScheduleDateValid() {
        ScheduleValidator scheduleValidator = new ScheduleValidator();
        ScheduleDto scheduleDto = createValidScheduleDto();
        Errors errors = new DirectFieldBindingResult(scheduleDto, "scheduleDto");
        scheduleValidator.validate(scheduleDto, errors);
        assertThat(errors.hasErrors()).isFalse();
    }

    @Test
    @DisplayName("세부 일정의 시작 날짜, 종료 날짜들의 유효성 실패 테스트")
    public void testScheduleDateInvalid() {
        ScheduleValidator scheduleValidator = new ScheduleValidator();
        ScheduleDto scheduleDto = createInvalidScheduleDto();
        Errors errors = new DirectFieldBindingResult(scheduleDto, "scheduleDto");
        scheduleValidator.validate(scheduleDto, errors);
        assertThat(errors.hasErrors()).isTrue();
    }

    //ScheduleValidator의 테스트 목적은 LocalDateTime객체들 간의 유효성을 검사하는 것이기 때문에 다른 데이터들에 대한 set은 생략한다.
    private static ScheduleDto createValidScheduleDto() {
        ScheduleDto scheduleDto = createScheduleDtoHolder();
        AtomicInteger day = new AtomicInteger(1);//schedule detail의 시작 날짜와 종료 날짜의 day에 사용될 변수
        for (ScheduleLocationDto scheduleLocationDto : scheduleDto.getScheduleLocationDtos()) {
            for (ScheduleDetailDto scheduleDetailDto : scheduleLocationDto.getScheduleDetailDtos()) {
                scheduleDetailDto.setStartDate(LocalDateTime.of(2020, 5, day.get() + 1, 12, 0, 0));
                scheduleDetailDto.setEndDate(LocalDateTime.of(2020, 5, day.get() + 2, 12, 0, 0));
                day.getAndIncrement();
            }
        }

        return scheduleDto;
    }

    //day를 기준으로 유효하지 않은 상세 일정 시간을 가진 schedule dto생성.
    private static ScheduleDto createInvalidScheduleDto() {
        ScheduleDto scheduleDto = createScheduleDtoHolder();
        AtomicInteger day = new AtomicInteger(1);//schedule detail의 시작 날짜와 종료 날짜의 day에 사용될 변수
        for (ScheduleLocationDto scheduleLocationDto : scheduleDto.getScheduleLocationDtos()) {
            for (ScheduleDetailDto scheduleDetailDto : scheduleLocationDto.getScheduleDetailDtos()) {
                scheduleDetailDto.setStartDate(LocalDateTime.of(2020, 5, day.get() + 2, 12, 0, 0));
                scheduleDetailDto.setEndDate(LocalDateTime.of(2020, 5, day.get() + 1, 12, 0, 0));
                day.getAndIncrement();
            }
        }

        return scheduleDto;
    }

    //Schedule dto 객체를 생성해주는 메소드
    private static ScheduleDto createScheduleDtoHolder() {

        LinkedHashSet<ScheduleLocationDto> scheduleLocationDtos = new LinkedHashSet<>();//일정 장소 dto set
        IntStream.range(0, 3).forEach(i -> {

            LinkedHashSet<ScheduleDetailDto> scheduleDetailDtos = new LinkedHashSet<>();//상세 일정 dto set
            IntStream.range(0, 3).forEach(j -> {
                ScheduleDetailDto scheduleDetailDto = ScheduleDetailDto.builder().place("place" + j).build();
                scheduleDetailDtos.add(scheduleDetailDto);
            });

            ScheduleLocationDto scheduleLocationDto = ScheduleLocationDto.builder().location("location" + i).scheduleDetailDtos(scheduleDetailDtos).build();
            scheduleLocationDtos.add(scheduleLocationDto);
        });

        ScheduleDto scheduleDto = ScheduleDto.builder().scheduleLocationDtos(scheduleLocationDtos).build();

        return scheduleDto;
    }
}
