package com.realityexpander.fakes

import com.realityexpander.data.models.ApiKey
import com.realityexpander.domain.security.auth.ApiKeyDataSource
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Fake implementation of [ApiKeyDataSource] for testing purposes.
 *
 * This class simulates the behavior of an API key data source by storing API keys in memory.
 * It provides methods to create, validate, and retrieve API keys.
 */
class ApiKeyDataSourceFake : ApiKeyDataSource {

    val apiKeys = mutableListOf<ApiKey>()

    override suspend fun createKey(key: String, email: String, validFrom: String) {
        val nowUtc = ZonedDateTime.now(ZoneId.of("UTC"))
        apiKeys.add(
            ApiKey(
                key = key,
                email = email,
                expiresAt = nowUtc.plusMonths(6L).toInstant().toString(),
                validFrom = validFrom
            )
        )
    }

    override suspend fun isValidKey(key: String): Boolean {
        val nowUtc = ZonedDateTime.now(ZoneId.of("UTC")).toInstant().toString()
        return apiKeys.any {
            it.key == key && it.validFrom <= nowUtc && it.expiresAt >= nowUtc
        }
    }

    override suspend fun getKeyByEmail(email: String): ApiKey? {
        return apiKeys.find { it.email == email }
    }
}
