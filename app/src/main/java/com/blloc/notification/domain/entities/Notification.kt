package com.blloc.notification.domain.entities

import java.time.LocalDateTime

data class Notification(
    val key: String,
    val appPackage: String,
    val text: String,
    val date: LocalDateTime
)
