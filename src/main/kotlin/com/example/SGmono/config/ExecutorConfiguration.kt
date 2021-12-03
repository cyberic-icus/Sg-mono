package com.example.SGmono.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
class ExecutorConfiguration {
    @Bean
    fun asyncExecutor(): ThreadPoolTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 4
        executor.maxPoolSize = 4
        executor.setQueueCapacity(500)
        executor.setThreadNamePrefix("crawler-")
        executor.initialize()
        return executor
    }
}