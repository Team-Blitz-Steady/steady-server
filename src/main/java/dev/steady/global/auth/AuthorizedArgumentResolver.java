package dev.steady.global.auth;

import dev.steady.global.exception.AuthenticationException;
import dev.steady.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static dev.steady.user.exception.UserErrorCode.USER_NOT_FOUND;

@Component
@RequiredArgsConstructor
public class AuthorizedArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthContext authContext;
    private final UserRepository userRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(Auth.class) != null
               && parameter.getParameterType().equals(UserInfo.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        UserInfo userInfo = UserInfo.from(authContext);

        if (userInfo.isAuthenticated() &&
            !userRepository.existsById(userInfo.userId())) {
            throw new AuthenticationException(USER_NOT_FOUND);
        }

        return userInfo;
    }

}
