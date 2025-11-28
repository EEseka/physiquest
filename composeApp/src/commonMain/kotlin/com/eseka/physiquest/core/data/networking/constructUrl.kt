package com.eseka.physiquest.core.data.networking

private const val NASA_BASE_URL = "https://api.nasa.gov/"

fun constructUrl(url: String): String {
    return when {
        url.contains(NASA_BASE_URL) -> url
        url.startsWith("/") -> NASA_BASE_URL + url.drop(1) // we drop the first '/' as the base url string already contains a '/' at the end
        else -> NASA_BASE_URL + url // Just in case we pass a path without the first slash
    }
}
