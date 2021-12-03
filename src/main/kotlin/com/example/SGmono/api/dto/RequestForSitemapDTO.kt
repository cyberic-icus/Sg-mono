package com.example.SGmono.api.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class RequestForSitemapDTO (
    @JsonProperty("baseURL")
    var baseURL: String? = null
    )