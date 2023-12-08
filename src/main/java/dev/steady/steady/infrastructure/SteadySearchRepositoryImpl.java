package dev.steady.steady.infrastructure;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.sql.JPASQLQuery;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLTemplates;
import dev.steady.global.auth.UserInfo;
import dev.steady.steady.domain.Participant;
import dev.steady.steady.domain.Steady;
import dev.steady.steady.domain.SteadyStatus;
import dev.steady.steady.dto.SearchConditionDto;
import dev.steady.steady.dto.response.MySteadyQueryResponse;
import dev.steady.user.domain.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

import static dev.steady.steady.domain.QParticipant.participant;
import static dev.steady.steady.domain.QSteady.steady;
import static dev.steady.steady.domain.QSteadyLike.steadyLike;
import static dev.steady.steady.domain.QSteadyPosition.steadyPosition;
import static dev.steady.steady.domain.QSteadyStack.steadyStack;
import static dev.steady.steady.domain.SteadyStatus.CLOSED;
import static dev.steady.steady.domain.SteadyStatus.FINISHED;
import static dev.steady.steady.domain.SteadyStatus.RECRUITING;
import static dev.steady.steady.infrastructure.util.DynamicQueryUtils.filterCondition;
import static dev.steady.steady.infrastructure.util.DynamicQueryUtils.orderBySort;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SteadySearchRepositoryImpl implements SteadySearchRepository {

    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;
    private final SQLTemplates sqlTemplates;

    @Override
    public Page<Steady> test(UserInfo userInfo, SearchConditionDto condition, Pageable pageable) {
        JPASQLQuery<?> jpaSqlQuery = new JPASQLQuery<>(entityManager, sqlTemplates);
        StringPath subQueryAlias = Expressions.stringPath("sub_query_steady");
        DateTimePath<LocalDateTime> promotedAt = Expressions.dateTimePath(LocalDateTime.class, steady, "promoted_at");

        List<Long> ids = jpaSqlQuery
                .select(steady.id)
                .distinct()
                .from(steady)
                .innerJoin(JPAExpressions.select(steady.id, promotedAt).distinct()
                                .from(steady)
                                .leftJoin(steadyLike).on(steady.id.eq(Expressions.numberPath(Long.class, steadyLike, "steady_id")))
                                .where(isLikedSteady(condition.like(), userInfo)),
                        subQueryAlias)
                .on(steady.id.eq(
                        Expressions.numberPath(Long.class, subQueryAlias, "id")
                ))
                .fetch();

        List<Steady> steadies = jpaQueryFactory
                .selectFrom(steady)
                .distinct()
                .innerJoin(steady.steadyStacks, steadyStack)
                .innerJoin(steadyPosition)
                .on(steady.id.eq(steadyPosition.steady.id))
                .where(steady.id.in(ids))
                .orderBy(orderBySort(pageable.getSort(), Steady.class))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Steady> count = jpaQueryFactory
                .selectFrom(steady)
                .distinct()
                .innerJoin(steady.steadyStacks, steadyStack)
                .innerJoin(steadyPosition)
                .on(steady.id.eq(steadyPosition.steady.id))
                .where(searchCondition(userInfo, condition));

//        JPAQuery<Steady> count = jpaQueryFactory
//                .selectFrom(steady)
//                .distinct()
//                .innerJoin(steady.steadyStacks, steadyStack)
//                .innerJoin(steadyPosition)
//                .on(steady.id.eq(steadyPosition.steady.id))
//                .leftJoin(steadyLike)
//                .on(steady.id.eq(steadyLike.steady.id))
//                .where(searchCondition(userInfo, condition));

        return PageableExecutionUtils.getPage(steadies, pageable, count::fetchCount);
    }

//    @Override
//    public Page<Steady> test(UserInfo userInfo, SearchConditionDto conditionDto, Pageable pageable) {
//        JPASQLQuery<?> jpaSqlQuery = new JPASQLQuery<>(entityManager, sqlTemplates);
//        StringPath subQueryAlias = Expressions.stringPath("sub_query_steady");
//        StringPath countSubQueryAlias = Expressions.stringPath("count_sub_query_steady");
//        DateTimePath<LocalDateTime> promotedAt = Expressions.dateTimePath(LocalDateTime.class, steady, "promoted_at");
//
//        List<Steady> steadies = jpaSqlQuery
//                .select(steady)
//                .distinct()
//                .from(steady)
//                .innerJoin(
//                        SQLExpressions.select(steady.id, promotedAt).distinct()
//                                .from(steady)
//                                .leftJoin(steadyLike).on(steady.id.eq(Expressions.numberPath(Long.class, steadyLike, "steady_id")))
//                                .where(isLikedSteady(conditionDto.like(), userInfo))
//                                .orderBy(promotedAt.desc())
//                                .offset(pageable.getOffset())
//                                .limit(pageable.getPageSize()),
//                        subQueryAlias
//                )
//                .on(steady.id.eq(
//                        Expressions.numberPath(Long.class, subQueryAlias, "id")
//                ))
//                .innerJoin(steadyStack)
//                .on(steady.id.eq(Expressions.numberPath(Long.class, steadyStack, "steady_id")))
//                .innerJoin(steadyPosition)
//                .on(steady.id.eq(Expressions.numberPath(Long.class, steadyPosition, "steady_id")))
//                .fetch();
//
//        JPASQLQuery<Steady> count = jpaSqlQuery
//                .select(steady)
//                .distinct()
//                .from(steady)
//                .innerJoin(
//                        SQLExpressions.select(steady.id, promotedAt).distinct()
//                                .from(steady)
//                                .leftJoin(steadyLike).on(steady.id.eq(Expressions.numberPath(Long.class, steadyLike, "steady_id")))
//                                .where(isLikedSteady(conditionDto.like(), userInfo)),
//                        countSubQueryAlias
//                )
//                .on(steady.id.eq(
//                        Expressions.numberPath(Long.class, countSubQueryAlias, "id")))
//                .innerJoin(steadyStack)
//                .on(steady.id.eq(Expressions.numberPath(Long.class, steadyStack, "steady_id")))
//                .innerJoin(steadyPosition)
//                .on(steady.id.eq(Expressions.numberPath(Long.class, steadyPosition, "steady_id")));
//
//        return PageableExecutionUtils.getPage(steadies, pageable, ()-> count.fetch().size());

    @Override
    public Page<Steady> findAllBySearchCondition(UserInfo userInfo, SearchConditionDto condition, Pageable pageable) {
        List<Steady> steadies = jpaQueryFactory
                .selectFrom(steady)
                .distinct()
                .innerJoin(steady.steadyStacks, steadyStack)
                .innerJoin(steadyPosition)
                .on(steady.id.eq(steadyPosition.steady.id))
                .leftJoin(steadyLike)
                .on(steady.id.eq(steadyLike.steady.id))
                .where(searchCondition(userInfo, condition))
                .orderBy(orderBySort(pageable.getSort(), Steady.class))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Steady> count = jpaQueryFactory
                .selectFrom(steady)
                .distinct()
                .innerJoin(steady.steadyStacks, steadyStack)
                .innerJoin(steadyPosition)
                .on(steady.id.eq(steadyPosition.steady.id))
                .leftJoin(steadyLike)
                .on(steady.id.eq(steadyLike.steady.id))
                .where(searchCondition(userInfo, condition));

        return PageableExecutionUtils.getPage(steadies, pageable, count::fetchCount);
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

    private BooleanBuilder searchCondition(UserInfo userInfo, SearchConditionDto condition) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
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

    private BooleanExpression isLikedSteady(boolean like, UserInfo userInfo) {
        if (like) {
            return steadyLike.user.id.eq(userInfo.userId());
        }
        return null;
    }

}
