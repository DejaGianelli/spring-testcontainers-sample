package com.example.demo;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

@ImportTestcontainers(MyContainers.class)
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

}
