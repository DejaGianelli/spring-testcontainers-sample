package com.example.demo;

import io.awspring.cloud.sqs.listener.interceptor.MessageInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

@ImportTestcontainers(MyContainers.class)
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(TestcontainersConfiguration.class);

    @Bean
    public TestMessageInterceptor<Object> testMessageInterceptor() {
        return new TestMessageInterceptor<Object>() {

            private final Map<String, Boolean> executionMap = new HashMap<>();

            @Override
            public void afterProcessing(Message<Object> message, Throwable t) {
                String messageId = message.getHeaders().getId().toString();
                this.executionMap.put(messageId, true);
                TestMessageInterceptor.super.afterProcessing(message, t);
                logger.info("Message {} processed", messageId);
            }

            @Override
            public void waitForMessageProcessed(String messageId) {
                this.executionMap.put(messageId, false);
                while (!executionMap.get(messageId)) {
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(200));
                }
            }
        };
    }

    interface TestMessageInterceptor<T> extends MessageInterceptor<T> {
        void waitForMessageProcessed(String messageId);
    }

}
