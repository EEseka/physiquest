package com.eseka.physiquest

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform