package me.moonsoo.travelerrestapi.schedule;

import me.moonsoo.commonmodule.account.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ScheduleService {

    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    ScheduleLocationRepository scheduleLocationRepository;

    @Autowired
    ScheduleDetailRepository scheduleDetailRepository;

    public Schedule save(Account account, Schedule schedule) {
        schedule.setAccount(account);
        schedule.setViewCount(0);
        schedule.setRegDate(LocalDateTime.now());
        Schedule savedSchedule = scheduleRepository.save(schedule);
        for (ScheduleLocation scheduleLocation : savedSchedule.getScheduleLocations()) {
            ScheduleLocation savedScheduleLocation = scheduleLocationRepository.save(scheduleLocation);
            for (ScheduleDetail scheduleDetail : savedScheduleLocation.getScheduleDetails()) {
                scheduleDetailRepository.save(scheduleDetail);
            }
        }
        return savedSchedule;
    }
}
