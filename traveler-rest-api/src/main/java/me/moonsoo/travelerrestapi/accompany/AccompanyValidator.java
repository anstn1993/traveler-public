package me.moonsoo.travelerrestapi.accompany;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class AccompanyValidator {

    public void validate(AccompanyDto accompanyDto, Errors errors) {
        //시작 시간이 끝나는 시간보다 늦는 경우
        if(accompanyDto.getStartDate().isAfter(accompanyDto.getEndDate())) {
            errors.rejectValue("endDate", "wrong value", "endDate should be later than startDate");
        }
    }


}
