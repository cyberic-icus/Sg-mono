package com.example.SGmono.helpers

import org.springframework.stereotype.Component
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

@Component
class UrlChecker {
    fun getResponseCodeForURL(address: String): Int {
        return getResponseCodeForURLUsing(address, "GET")
    }

    fun getResponseCodeForURLUsingHead(address: String): Int {
        return getResponseCodeForURLUsing(address, "HEAD")
    }

    private fun getResponseCodeForURLUsing(address: String, method: String): Int {
        HttpURLConnection.setFollowRedirects(false)
        val url = URL(address)
        val huc = url.openConnection() as HttpURLConnection
        huc.requestMethod = method
        return huc.responseCode
    }
}