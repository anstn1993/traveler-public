package me.moonsoo.travelerrestapi.schedule;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleLocationRepository extends JpaRepository<ScheduleLocation, Integer> {
    List<ScheduleLocation> findAllBySchedule(Schedule schedule);

}
