package com.realityexpander.domain.agenda

interface KilledTokenDataSource {
    suspend fun insertKilledToken(token: String): Boolean
    suspend fun isKilled(token: String): Boolean
}