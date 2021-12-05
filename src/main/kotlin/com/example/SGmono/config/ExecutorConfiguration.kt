package com.example.SGmono.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
@EnableAsync
@ComponentScan("com.example.service")
class ExecutorConfiguration {
    @Bean(name = ["threadPoolTaskExecutor"])
    fun threadPoolTaskExecutor(): ThreadPoolTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 4
        executor.maxPoolSize = 4
        executor.setQueueCapacity(500)
        executor.setThreadNamePrefix("crawler-")
        executor.initialize()
        return executor
    }
}