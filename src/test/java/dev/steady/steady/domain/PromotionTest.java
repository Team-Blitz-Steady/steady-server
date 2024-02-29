package dev.steady.steady.domain;

import dev.steady.global.exception.InvalidStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PromotionTest {

    @Test
    @DisplayName("프로모션은 3회만 가능하다.")
    void promotionUseTest() {
        // given
        Promotion promotion = new Promotion();
        int limit = 3;
        while (limit-- > 0) {
            promotion.use();
        }

        // when & then
        assertThatThrownBy(() -> promotion.use())
                .isInstanceOf(InvalidStateException.class);
    }

}
