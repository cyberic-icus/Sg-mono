package com.example.SGmono.helpers

import org.springframework.stereotype.Service

@Service
class UrlNormalizer {

    fun siteNameNormalizer(siteName: String) =
        siteName
            .replace(".", "")
            .replace("https://www", "")
            .replace("http://www", "")
            .replace("/", "")
}