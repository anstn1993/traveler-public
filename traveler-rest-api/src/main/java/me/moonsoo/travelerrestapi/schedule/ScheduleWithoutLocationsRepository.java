package me.moonsoo.travelerrestapi.schedule;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleWithoutLocationsRepository extends JpaRepository<ScheduleWithoutLocations, Integer>, ScheduleWithoutLocationsCustomRepository {
}
