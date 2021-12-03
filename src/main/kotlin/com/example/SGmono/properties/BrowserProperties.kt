package com.example.SGmono.properties

import lombok.Getter
import lombok.Setter
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "browser")
@EnableConfigurationProperties
class BrowserProperties {
    lateinit var path: String
}