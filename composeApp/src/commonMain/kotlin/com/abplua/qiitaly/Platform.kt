package com.abplua.qiitaly

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform