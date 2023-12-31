package dev.steady.global.config;

import dev.steady.global.logging.MDCRequestHandlerCommentInterceptor;
import lombok.RequiredArgsConstructor;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("dev")
@Configuration
@RequiredArgsConstructor
public class HibernateConfig {

    private final MDCRequestHandlerCommentInterceptor mdcRequestHandlerCommentInterceptor;

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertyConfig() {
        return hibernateProperties ->
                hibernateProperties.put(AvailableSettings.STATEMENT_INSPECTOR, mdcRequestHandlerCommentInterceptor);
    }

}
