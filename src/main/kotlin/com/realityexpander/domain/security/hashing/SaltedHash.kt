package com.realityexpander.domain.security.hashing

data class SaltedHash(
    val hash: String,
    val salt: String
)
