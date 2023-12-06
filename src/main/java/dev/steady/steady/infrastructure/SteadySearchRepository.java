package dev.steady.steady.infrastructure;

import dev.steady.global.auth.UserInfo;
import dev.steady.steady.domain.Steady;
import dev.steady.steady.domain.SteadyStatus;
import dev.steady.steady.dto.SearchConditionDto;
import dev.steady.steady.dto.response.MySteadyQueryResponse;
import dev.steady.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface SteadySearchRepository {

    Page<Steady> findAllBySearchCondition(UserInfo userInfo, SearchConditionDto condition, Pageable pageable);

    Slice<MySteadyQueryResponse> findMySteadies(SteadyStatus status, User user, Pageable pageable);

    Page<Steady> test(UserInfo userInfo, SearchConditionDto condition, Pageable pageable);
}
