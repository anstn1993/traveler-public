package me.moonsoo.travelerrestapi.schedule;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import me.moonsoo.commonmodule.account.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.util.List;

import static me.moonsoo.travelerrestapi.follow.QFollow.follow;
import static me.moonsoo.travelerrestapi.schedule.QScheduleLocation.scheduleLocation;
import static me.moonsoo.travelerrestapi.schedule.QScheduleWithoutLocations.scheduleWithoutLocations;

//Schedule 목록을 조건 별로 조회하기 위한 커스텀 repository
@Repository
public class ScheduleWithoutLocationsCustomRepositoryImpl extends QuerydslRepositorySupport implements ScheduleWithoutLocationsCustomRepository {

    @Autowired
    private JPAQueryFactory queryFactory;

    public ScheduleWithoutLocationsCustomRepositoryImpl() {
        super(ScheduleWithoutLocations.class);
    }

    //인증상태에서 일정 목록을 조회
    @Override
    public Page<ScheduleWithoutLocations> findAllWithAuth(Account account, String filter, String search, Pageable pageable) {

        //Predicate객체를 cascading방식으로 동적으로 생성
        BooleanBuilder defaultPredicateBuilder = new BooleanBuilder();
        defaultPredicateBuilder.or(scheduleWithoutLocations.scope.in(Scope.ALL))
                .or(scheduleWithoutLocations.scope.eq(Scope.FOLLOWER).and(follow.followingAccount.eq(account)))
                .or(scheduleWithoutLocations.scope.eq(Scope.FOLLOWER).and(scheduleWithoutLocations.account.eq(account)))
                .or(scheduleWithoutLocations.scope.eq(Scope.NONE).and(scheduleWithoutLocations.account.eq(account)))
        ;

        JPAQuery<ScheduleWithoutLocations> query = queryFactory.selectFrom(scheduleWithoutLocations)
                .leftJoin(follow).on(scheduleWithoutLocations.account.eq(follow.followedAccount))
                .groupBy(scheduleWithoutLocations.id);

        if(filter == null || filter.isBlank() || search == null || search.isBlank()) {
            query.where(defaultPredicateBuilder);
        }
        else if(filter.equals("writer")) {
            query.where(defaultPredicateBuilder.and(scheduleWithoutLocations.account.nickname.containsIgnoreCase(search)));
        }
        else if(filter.equals("title")) {
            query.where(defaultPredicateBuilder.and(scheduleWithoutLocations.title.containsIgnoreCase(search)));
        }
        else if(filter.equals("location")) {
            query.join(scheduleLocation).on(scheduleWithoutLocations.id.eq(scheduleLocation.schedule.id))
                    .where(defaultPredicateBuilder.and(scheduleLocation.location.containsIgnoreCase(search)));
        }

        List<ScheduleWithoutLocations> schedules = getQuerydsl().applyPagination(pageable, query).fetch();
        return new PageImpl<>(schedules, pageable, query.fetchCount());
    }

    //미인증 상태에서 일정 목록 조회
    @Override
    public Page<ScheduleWithoutLocations> findAllWithoutAuth(String filter, String search, Pageable pageable) {
        JPAQuery<ScheduleWithoutLocations> query = queryFactory.selectFrom(scheduleWithoutLocations);
        BooleanExpression defaultPredicate = scheduleWithoutLocations.scope.eq(Scope.ALL);//모든 쿼리에 들어가는 조건
        if (filter == null || filter.isBlank() || search == null || search.isBlank()) {//미인증, 유효하지 않은 검색 필터링 param
            query.where(defaultPredicate);
        } else if (filter.equals("writer")) {
            query.where(defaultPredicate.and(scheduleWithoutLocations.account.nickname.containsIgnoreCase(search)));
        } else if (filter.equals("title")) {
            query.where(defaultPredicate.and(scheduleWithoutLocations.title.containsIgnoreCase(search)));
        } else if (filter.equals("location")) {
            query.leftJoin(scheduleLocation).on(scheduleWithoutLocations.id.eq(scheduleLocation.schedule.id))
                    .where(defaultPredicate.and(scheduleLocation.location.containsIgnoreCase(search)))
                    .groupBy(scheduleWithoutLocations.id);
        }

        List<ScheduleWithoutLocations> schedules = getQuerydsl().applyPagination(pageable, query).fetch();
        return new PageImpl<>(schedules, pageable, query.fetchCount());
    }

}
