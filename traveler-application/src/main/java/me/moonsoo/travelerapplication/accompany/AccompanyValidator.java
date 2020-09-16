package me.moonsoo.travelerapplication.accompany;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class AccompanyValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(AccompanyDto.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        AccompanyDto accompanyDto = (AccompanyDto) target;
        if(accompanyDto.getEndDate().isBefore(accompanyDto.getStartDate())) {
            errors.rejectValue("endDate", "wrong value", "endDate should be later than startDate");
        }
    }
}
