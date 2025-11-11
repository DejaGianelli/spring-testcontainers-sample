package com.example.demo;

import io.awspring.cloud.sqs.listener.interceptor.MessageInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.BindMode;
import org.testcontainers.localstack.LocalStackContainer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

@ImportTestcontainers(MyContainers.class)
@TestConfiguration(proxyBeanMethods = false)
class IntegrationTestsConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(IntegrationTestsConfiguration.class);

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

    @Bean
    LocalStackContainer localStackContainer() {
        try (LocalStackContainer localStackContainer =
                     new LocalStackContainer("localstack/localstack:3.5.0")
                             .withServices("sqs")
                             .withFileSystemBind(
                                     ".docker/localstack",
                                     "/etc/localstack/init/ready.d",
                                     BindMode.READ_ONLY
                             )) {
            return localStackContainer;
        }
    }

    @Bean
    DynamicPropertyRegistrar apiPropertiesRegistrar(LocalStackContainer localStackContainer) {
        return registry -> {
            registry.add("spring.cloud.aws.sqs.endpoint", localStackContainer::getEndpoint);
            registry.add("spring.cloud.aws.credentials.access-key", localStackContainer::getAccessKey);
            registry.add("spring.cloud.aws.credentials.secret-key", localStackContainer::getSecretKey);
            registry.add("spring.cloud.aws.region.static", localStackContainer::getRegion);
        };
    }

    interface TestMessageInterceptor<T> extends MessageInterceptor<T> {
        void waitForMessageProcessed(String messageId);
    }
}
