package com.abplus.qiitaly

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform