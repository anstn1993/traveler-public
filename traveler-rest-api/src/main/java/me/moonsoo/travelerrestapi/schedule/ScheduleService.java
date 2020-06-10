package me.moonsoo.travelerrestapi.schedule;

import com.querydsl.core.types.Predicate;
import me.moonsoo.commonmodule.account.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ScheduleService {

    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    ScheduleLocationRepository scheduleLocationRepository;

    @Autowired
    ScheduleDetailRepository scheduleDetailRepository;

    @Autowired
    ScheduleWithoutLocationsRepository scheduleWithoutLocationsRepository;

    @Transactional
    public Schedule save(Account account, Schedule schedule) {
        schedule.setAccount(account);
        schedule.setViewCount(0);
        schedule.setRegDate(LocalDateTime.now());
        Schedule savedSchedule = scheduleRepository.save(schedule);
        for (ScheduleLocation scheduleLocation : savedSchedule.getScheduleLocations()) {
            scheduleLocation.setSchedule(schedule);
            ScheduleLocation savedScheduleLocation = scheduleLocationRepository.save(scheduleLocation);
            for (ScheduleDetail scheduleDetail : savedScheduleLocation.getScheduleDetails()) {
                scheduleDetail.setScheduleLocation(savedScheduleLocation);
                scheduleDetailRepository.save(scheduleDetail);
            }
        }
        return savedSchedule;
    }

    public Page<ScheduleWithoutLocations> findAll(Pageable pageable, Account account, Map<String, String> params) {
        String filter = params.get("filter");
        String search = params.get("search");

        if (account == null) {
            return scheduleWithoutLocationsRepository.findAllWithoutAuth(filter, search, pageable);
        } else {
            return scheduleWithoutLocationsRepository.findAllWithAuth(account, filter, search, pageable);
        }
    }

    @Transactional
    public Schedule update(Schedule schedule) {
        schedule.getScheduleLocations().forEach(scheduleLocation -> {
            scheduleLocation.getScheduleDetails().forEach(scheduleDetail -> {
                scheduleDetail.setScheduleLocation(scheduleLocation);
                scheduleDetailRepository.save(scheduleDetail);//schedule detail save
            });
            scheduleLocation.setSchedule(schedule);
            scheduleLocationRepository.save(scheduleLocation);//schedule location save
        });
        return scheduleRepository.save(schedule);
    }

    public void delete(Schedule schedule) {
        scheduleRepository.delete(schedule);
    }

    public Schedule updateViewCount(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    @Transactional
    public void deleteScheduleLocations(Schedule schedule) {
        List<ScheduleLocation> scheduleLocations = scheduleLocationRepository.findAllBySchedule(schedule);
        scheduleLocationRepository.deleteAll(scheduleLocations);
    }
}
