package com.hxht.mobile.committee.entity

import java.io.Serializable

data class User(
        val id: Long,
        val username: String,
        val password: String
) : Serializable