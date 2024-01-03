package dev.steady.steady.infrastructure;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.steady.global.auth.UserInfo;
import dev.steady.steady.domain.Participant;
import dev.steady.steady.domain.Steady;
import dev.steady.steady.domain.SteadyStatus;
import dev.steady.steady.domain.SteadyType;
import dev.steady.steady.dto.FilterConditionDto;
import dev.steady.steady.dto.RankCondition;
import dev.steady.steady.dto.RankType;
import dev.steady.steady.dto.response.MySteadyQueryResponse;
import dev.steady.steady.uitl.Cursor;
import dev.steady.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

import static dev.steady.steady.domain.QParticipant.participant;
import static dev.steady.steady.domain.QSteady.steady;
import static dev.steady.steady.domain.QSteadyLike.steadyLike;
import static dev.steady.steady.domain.QSteadyPosition.steadyPosition;
import static dev.steady.steady.domain.QSteadyStack.steadyStack;
import static dev.steady.steady.domain.SteadyStatus.CLOSED;
import static dev.steady.steady.domain.SteadyStatus.FINISHED;
import static dev.steady.steady.domain.SteadyStatus.RECRUITING;
import static dev.steady.steady.dto.RankType.PROJECT;
import static dev.steady.steady.dto.RankType.STUDY;
import static dev.steady.steady.infrastructure.util.DynamicQueryUtils.filterCondition;
import static dev.steady.steady.infrastructure.util.DynamicQueryUtils.orderBySort;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SteadyQueryRepositoryImpl implements SteadyQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Steady> findAllByFilterCondition(UserInfo userInfo, FilterConditionDto condition, Pageable pageable) {
        List<Steady> steadies = jpaQueryFactory
                .selectFrom(steady)
                .distinct()
                .innerJoin(steadyStack)
                .on(steady.id.eq(steadyStack.steady.id))
                .innerJoin(steadyPosition)
                .on(steady.id.eq(steadyPosition.steady.id))
                .leftJoin(steadyLike)
                .on(steady.id.eq(steadyLike.steady.id))
                .where(filterConditionBuilder(userInfo, condition))
                .orderBy(orderBySort(pageable.getSort(), Steady.class))
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(steadies, pageable, steadies.size());
    }

    @Override
    public Slice<MySteadyQueryResponse> findMySteadies(SteadyStatus status, User user, Pageable pageable) {
        List<MySteadyQueryResponse> steadies = jpaQueryFactory
                .select(Projections.constructor(MySteadyQueryResponse.class,
                        steady.id,
                        steady.name,
                        steady.contact,
                        participant.isLeader,
                        participant.createdAt))
                .from(steady)
                .innerJoin(participant).on(participant.steady.id.eq(steady.id))
                .where(
                        isFinishedSteady(status),
                        isWorkSteady(status),
                        isParticipantUserIdEqual(user),
                        isParticipantNotDeleted()
                )
                .orderBy(orderBySort(pageable.getSort(), Participant.class), steady.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = steadies.size() > pageable.getPageSize();
        List<MySteadyQueryResponse> content = hasNext ? steadies.subList(0, steadies.size() - 1) : steadies;

        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    public List<Steady> findPopularStudyInCondition(RankCondition condition) {
        return jpaQueryFactory.selectFrom(steady)
                .where(steady.promotion.promotedAt
                                .after(condition.date().atStartOfDay()),
                        steadyTypeCond(condition.type())
                )
                .orderBy(steady.likeCount.desc(), steady.viewCount.desc())
                .limit(condition.limit())
                .fetch();
    }

    private BooleanExpression isParticipantUserIdEqual(User user) {
        return participant.user.id.eq(user.getId());
    }

    private BooleanExpression isParticipantNotDeleted() {
        return participant.isDeleted.isFalse();
    }

    private BooleanExpression isFinishedSteady(SteadyStatus status) {
        if (FINISHED == status) {
            return steady.status.eq(FINISHED);
        }
        return null;
    }

    private BooleanExpression isWorkSteady(SteadyStatus status) {
        if (status == RECRUITING || status == CLOSED) {
            return steady.status.eq(SteadyStatus.RECRUITING)
                    .or(steady.status.eq(CLOSED));
        }
        return null;
    }

    private BooleanExpression steadyTypeCond(RankType type) {
        if (type == PROJECT) {
            return steady.type.eq(SteadyType.PROJECT);
        }
        if (type == STUDY) {
            return steady.type.eq(SteadyType.STUDY);
        }
        return null;
    }

    private BooleanBuilder filterConditionBuilder(UserInfo userInfo, FilterConditionDto condition) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        Cursor cursor = condition.cursor();
        if (Objects.isNull(cursor.getPromotedAt())) {
            booleanBuilder.and(filterCondition(cursor.getDeadline(), steady.deadline::goe));
        }
        booleanBuilder.and(filterCondition(cursor.getPromotedAt(), steady.promotion.promotedAt::lt));

        booleanBuilder.and(filterCondition(condition.steadyType(), steady.type::eq));
        booleanBuilder.and(filterCondition(condition.steadyMode(), steady.steadyMode::eq));
        booleanBuilder.and(filterCondition(condition.stacks(), steadyStack.stack.name::in));
        booleanBuilder.and(filterCondition(condition.positions(), steadyPosition.position.name::in));
        booleanBuilder.and(filterCondition(condition.status(), steady.status::eq));
        if (condition.like()) {
            booleanBuilder.and(filterCondition(userInfo.userId(), steadyLike.user.id::eq));
        }
        if (StringUtils.hasText(condition.keyword())) {
            return booleanBuilder.and(filterCondition(condition.keyword(), steady.title::contains))
                    .or(filterCondition(condition.keyword(), steady.content::contains));
        }

        return booleanBuilder;
    }

}
