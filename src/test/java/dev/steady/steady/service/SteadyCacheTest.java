package dev.steady.steady.service;

import dev.steady.steady.domain.Steady;
import dev.steady.steady.domain.repository.ParticipantRepository;
import dev.steady.steady.domain.repository.SteadyPositionRepository;
import dev.steady.steady.domain.repository.SteadyQuestionRepository;
import dev.steady.steady.domain.repository.SteadyRepository;
import dev.steady.steady.domain.repository.SteadyStackRepository;
import dev.steady.steady.dto.FilterConditionDto;
import dev.steady.steady.dto.request.SteadySearchRequest;
import dev.steady.steady.dto.response.PageResponse;
import dev.steady.steady.dto.response.SteadyQueryResponse;
import dev.steady.user.domain.Position;
import dev.steady.user.domain.Stack;
import dev.steady.user.domain.User;
import dev.steady.user.domain.repository.PositionRepository;
import dev.steady.user.domain.repository.StackRepository;
import dev.steady.user.domain.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import static dev.steady.global.auth.AuthFixture.createUserInfo;
import static dev.steady.steady.fixture.SteadyFixturesV2.createCacheableRequest;
import static dev.steady.steady.fixture.SteadyFixturesV2.createDefaultSteadySearchRequest;
import static dev.steady.steady.fixture.SteadyFixturesV2.createSteady;
import static dev.steady.steady.fixture.SteadyFixturesV2.createSteadyEntity;
import static dev.steady.steady.fixture.SteadyFixturesV2.generateSteadyCreateRequest;
import static dev.steady.user.fixture.UserFixturesV2.generatePosition;
import static dev.steady.user.fixture.UserFixturesV2.generateStack;
import static dev.steady.user.fixture.UserFixturesV2.generateUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.verify;

@Transactional
@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class SteadyCacheTest {

    @Autowired
    private SteadyService steadyService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StackRepository stackRepository;

    @Autowired
    private PositionRepository positionRepository;

    @SpyBean
    private SteadyRepository steadyRepository;

    private Position position;
    private Stack stack;
    private User leader;

    @BeforeEach
    void setUp() {
        position = positionRepository.save(generatePosition());
        stack = stackRepository.save(generateStack());
        leader = userRepository.save(generateUser(position));
    }

    @Test
    @DisplayName("스테디 목록 조회 요청이 이전과 동일하면 캐싱처리된다.")
    void getSteadiesSearchCachingTest() {
        // given
        var userInfo = createUserInfo(leader.getId());
        var steadyRequest = generateSteadyCreateRequest(stack.getId(), position.getId());

        steadyService.create(steadyRequest, userInfo);

        // when
        SteadySearchRequest searchRequest = createCacheableRequest();
        FilterConditionDto condition = FilterConditionDto.from(searchRequest);
        Pageable pageable = searchRequest.toPageable();
        steadyService.getSteadies(userInfo, condition, pageable);
        steadyService.getSteadies(userInfo, condition, pageable);

        // then
        verify(steadyRepository, atMostOnce()).findAllByFilterCondition(userInfo, condition, pageable);
    }

}
