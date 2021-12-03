package com.example.SGmono.service

import com.example.SGmono.properties.FileStorageProperties
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class AsyncTaskExecutorService(
    private val fileStorageService: FileStorageService,
    private val sitemapGeneratorService: SitemapGeneratorService,
    private val fileStorageProperties: FileStorageProperties

) {

    @Async
    fun createSitemapAsync(siteName: String): CompletableFuture<Void> {
        fileStorageService.writeToFile(
            "Лол кек чебурек", fileStorageProperties.path+"мяу.txt"
        )
        println(fileStorageProperties.path+"мяу.txt")

        val sitemap = sitemapGeneratorService.getMap(siteName)

        println(sitemap!!.joinToString("\n"))
        println(fileStorageProperties.path+siteNameNormalizer(siteName)+".txt")
        println(siteNameNormalizer(siteName)+".txt")
        fileStorageService.writeToFile(
            sitemap!!.joinToString("\n"), fileStorageProperties.path+siteNameNormalizer(siteName)+".txt"
        )

        return CompletableFuture.allOf()
    }

    private fun siteNameNormalizer(siteName: String) = siteName.replace(".", "")
}