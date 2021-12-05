package com.example.SGmono.service

import com.example.SGmono.helpers.UrlNormalizer
import com.example.SGmono.properties.FileStorageProperties
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Component
@EnableAsync
class AsyncTaskExecutorService(
    private val fileStorageService: FileStorageService,
    private val sitemapGeneratorService: SitemapGeneratorService,
    private val fileStorageProperties: FileStorageProperties,
    private val urlNormalizer: UrlNormalizer

) {

    @Async("threadPoolTaskExecutor")
    fun createSitemapAsync(siteName: String): CompletableFuture<Void> {
        val sitemap = sitemapGeneratorService.getMap(siteName)

        fileStorageService.writeToFile(
            sitemap?.joinToString("\n") ?: "No data available.",
            fileStorageProperties.path + urlNormalizer.siteNameNormalizer(siteName) + ".txt"
        )

        return CompletableFuture.allOf()
    }
}