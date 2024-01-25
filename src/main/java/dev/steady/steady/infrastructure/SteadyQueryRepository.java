package dev.steady.steady.infrastructure;

import dev.steady.global.auth.UserInfo;
import dev.steady.steady.dto.RankCondition;
import dev.steady.steady.domain.Steady;
import dev.steady.steady.domain.SteadyStatus;
import dev.steady.steady.dto.FilterConditionDto;
import dev.steady.steady.dto.response.MySteadyQueryResponse;
import dev.steady.steady.dto.response.SteadyFilterResponse;
import dev.steady.user.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface SteadyQueryRepository {

    SteadyFilterResponse findAllByFilterCondition(UserInfo userInfo, FilterConditionDto condition, Pageable pageable);

    Slice<MySteadyQueryResponse> findMySteadies(SteadyStatus status, User user, Pageable pageable);

    List<Steady> findPopularStudyInCondition(RankCondition condition);

}
