package me.moonsoo.travelerrestapi.schedule;

import io.lettuce.core.dynamic.annotation.Param;
import me.moonsoo.commonmodule.account.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ScheduleWithoutLocationsCustomRepository {
    Page<ScheduleWithoutLocations> findAllWithAuth(Account account, String filter, String search, Pageable pageable);
    Page<ScheduleWithoutLocations> findAllWithoutAuth(String filter, String search, Pageable pageable);
}
