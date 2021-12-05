package com.example.SGmono.api.controller

import com.example.SGmono.api.dto.RequestForSitemapDTO
import com.example.SGmono.helpers.UrlChecker
import com.example.SGmono.helpers.UrlNormalizer
import com.example.SGmono.properties.FileStorageProperties
import com.example.SGmono.service.AsyncTaskExecutorService
import com.example.SGmono.service.FileStorageService
import com.example.SGmono.service.SitemapGeneratorService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.InputStream
import org.springframework.http.ContentDisposition
import java.io.IOException

import org.springframework.web.bind.annotation.PathVariable

import javax.servlet.http.HttpServletResponse

import javax.servlet.http.HttpServletRequest

import org.springframework.web.bind.annotation.RequestMapping
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.fileSize


@RestController
@RequestMapping("/api/")
class SitemapController(
    private val fileStorageService: FileStorageService,
    private val sitemapGeneratorService: SitemapGeneratorService,
    private val asyncTaskExecutorService: AsyncTaskExecutorService,
    private val urlChecker: UrlChecker,
    private val fileStorageProperties: FileStorageProperties,
    private val urlNormalizer: UrlNormalizer
) {
    companion object {
        const val httpProtocol = "https://www."
        const val noSuchFileMessage = "No such file."
    }

    @PostMapping("request/")
    fun requestForSitemap(@RequestBody requestForSitemapDTO: RequestForSitemapDTO): String? =
        runCatching {
            val url = validateUrl(requestForSitemapDTO.baseURL)

            asyncTaskExecutorService.createSitemapAsync(
                url
            )
            ""
        }.onFailure {
            it.printStackTrace()
            return "lmao some shit happend"
        }.getOrNull()

    @GetMapping(
        value = ["/sitemap/{siteName}"],
        produces = ["text/plain"]
    )
    fun getFileWithSitemap(
        @PathVariable siteName: String
    ): ByteArray? =
        runCatching {
            val file = File(
                "${fileStorageProperties.path}$siteName.txt")
            if (file.exists()) {
                val inputStream = FileInputStream(file)
                inputStream.use {
                    it.readAllBytes()
                }
            } else noSuchFileMessage.toByteArray()
        }.onFailure {
        }.getOrNull()

    private fun validateUrl(site: String): String {
        val url = "$httpProtocol${site}"
        urlChecker.urlCheckup(url)
        return url
    }

}