package com.example.SGmono

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
class SgMonoApplication

fun main(args: Array<String>) {
	runApplication<SgMonoApplication>(*args)
}
