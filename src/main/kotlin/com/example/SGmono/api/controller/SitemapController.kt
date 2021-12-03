package com.example.SGmono.api.controller

import com.example.SGmono.api.dto.RequestForSitemapDTO
import com.example.SGmono.exception.UrlNotSpecifiedException
import com.example.SGmono.helpers.UrlChecker
import com.example.SGmono.service.AsyncTaskExecutorService
import com.example.SGmono.service.FileStorageService
import com.example.SGmono.service.SitemapGeneratorService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.Exception

@RestController
@RequestMapping("/api/")
class SitemapController(
    private val fileStorageService: FileStorageService,
    private val sitemapGeneratorService: SitemapGeneratorService,
    private val asyncTaskExecutorService: AsyncTaskExecutorService,
    private val urlChecker: UrlChecker
) {
    companion object{
        const val httpProtocol = "https://www."
    }

    @PostMapping("request/")
    fun requestForSitemap(@RequestBody requestForSitemapDTO: RequestForSitemapDTO): String? =
        runCatching{
            val url = httpProtocol+requestForSitemapDTO.baseURL
            if (
                requestForSitemapDTO.baseURL != null &&
                urlChecker.getResponseCodeForURLUsingHead(
                    url
                ) != 200
            ) throw RuntimeException("Site ${requestForSitemapDTO.baseURL} cannot be reached!")

            asyncTaskExecutorService.createSitemapAsync(
                url
            )
            // todo - shitsaving fileservice
            ""
        }.onFailure {
        }.getOrNull()
}