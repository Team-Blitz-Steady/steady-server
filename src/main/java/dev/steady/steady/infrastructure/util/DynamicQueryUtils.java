package dev.steady.steady.infrastructure.util;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static dev.steady.steady.infrastructure.util.StringConverterUtils.lowercaseFirstLetter;

public class DynamicQueryUtils {

    private DynamicQueryUtils() {
    }

    public static <T> BooleanExpression filterCondition(T condition, Function<T, BooleanExpression> function) {
        T conditionResult = condition;

        if (condition instanceof String str && !StringUtils.hasText(str)) {
            conditionResult = null;
        }

        if (condition instanceof List list && CollectionUtils.isEmpty(list)) {
            conditionResult = null;
        }

        return Optional.ofNullable(conditionResult)
                .map(function)
                .orElse(null);
    }

    public static <T> OrderSpecifier orderBySort(Sort sort, Class<T> clazz) {
        for (Sort.Order order : sort) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            String property = order.getProperty();

            String className = lowercaseFirstLetter(clazz.getSimpleName());
            PathBuilder<T> target = new PathBuilder<>(clazz, className);
            return new OrderSpecifier(direction, target.get(property));
        }
        return null;
    }

    public static <T> OrderSpecifier reverseOrderBySort(Sort sort, Class<T> clazz) {
        for (Sort.Order order : sort) {
            Order direction = order.isAscending() ? Order.DESC : Order.ASC;
            String property = order.getProperty();

            String className = lowercaseFirstLetter(clazz.getSimpleName());
            PathBuilder<T> target = new PathBuilder<>(clazz, className);
            return new OrderSpecifier(direction, target.get(property));
        }
        return null;
    }

}
