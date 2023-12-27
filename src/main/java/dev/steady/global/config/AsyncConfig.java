package dev.steady.global.config;

import dev.steady.notification.exception.NotificationAsyncExceptionHandler;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Bean(name = "EVENT_HANDLER_TASK_EXECUTOR")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(30);
        executor.setQueueCapacity(100);
        executor.setWaitForTasksToCompleteOnShutdown(true); // 시세틈을 종료할 때 queue에 남아있는 작업 모두 완료
        executor.setAwaitTerminationSeconds(10); // 타임아웃
        executor.setThreadNamePrefix("EVENT_EXECUTOR-");
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new NotificationAsyncExceptionHandler();
    }

}