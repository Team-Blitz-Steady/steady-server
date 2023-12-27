package dev.steady.user.service;

import dev.steady.user.domain.Stack;
import dev.steady.user.domain.repository.StackRepository;
import dev.steady.user.dto.response.StacksResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static dev.steady.user.fixture.UserFixtures.createStacks;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class StackServiceTest {

    @Autowired
    private StackService stackService;

    @Autowired
    private StackRepository stackRepository;

    @Test
    @DisplayName("모든 스택을 가져올 수 있다.")
    void getStacksTest() {
        // given
        List<Stack> stacks = createStacks();
        stackRepository.saveAll(stacks);

        // when
        StacksResponse response = stackService.getStacks();

        // then
        assertThat(response.stacks()).hasSameSizeAs(stacks);
    }

}
