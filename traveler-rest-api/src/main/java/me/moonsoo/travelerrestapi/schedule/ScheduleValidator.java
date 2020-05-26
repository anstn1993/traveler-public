package me.moonsoo.travelerrestapi.schedule;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

@Component
public class ScheduleValidator {

    public void validate(ScheduleDto scheduleDto, Errors errors) {

        //일정 엔티티에서 세부 일정의 시작 날짜. 종료 날짜만 뽑아서 리스트를 만들어준다.
        List<LocalDateTime> scheduleDetailDates = new ArrayList<>();
        for (ScheduleLocationDto scheduleLocationDto : scheduleDto.getScheduleLocationDtos()) {
            for (ScheduleDetailDto scheduleDetailDto : scheduleLocationDto.getScheduleDetailDtos()) {
                scheduleDetailDates.add(scheduleDetailDto.getStartDate());
                scheduleDetailDates.add(scheduleDetailDto.getEndDate());
            }
        }

        //LocalDateTime List에서 앞의 요소가 뒤의 요소보다 시간이 빠른지 검사하고 그렇지 않으면 errors에 error를 추가한다.
        LocalDateTime offsetDate = null;//비교의 대상이 되는 LocalDateTime객체를 담을 변수
        ListIterator<LocalDateTime> localDateTimeListIterator = scheduleDetailDates.listIterator();
        checkDateValidity(offsetDate, localDateTimeListIterator, errors);
    }

    public void checkDateValidity(LocalDateTime offsetDate, ListIterator<LocalDateTime> dateIterator, Errors errors) {
        //첫 번째 재귀 루프일 때 offsetDate는 null이기 때문에 반복자의 첫 번째 요소를 넣어준다.
        if(offsetDate == null) {
            offsetDate = dateIterator.next();
        }

        //반복자에 요소가 없으면 메소드 탈출
        if(!dateIterator.hasNext()) {
            return;
        }

        LocalDateTime targetDate = dateIterator.next();//offsetDate와 비교될 Date
        //offsetDate는 항상 targetDate보다 시간이 빨라야 하는데 그렇지 않은 경우 errors에 추가
        if(offsetDate.isAfter(targetDate)) {
            errors.reject("invalid error"
                    , "Your schedule detail date time is invalid. Check if your start date is later than end date in a schedule detail or your end date is later than start date of next schedule detail.");
            return;
        }
        offsetDate = targetDate;//offsetDate를 다음 요소로 바꿔준다.
        checkDateValidity(offsetDate, dateIterator, errors);
    }


}
