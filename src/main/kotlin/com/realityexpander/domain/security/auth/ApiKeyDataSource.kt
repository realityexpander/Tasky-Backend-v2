package com.realityexpander.domain.security.auth

import com.realityexpander.data.models.ApiKey

interface ApiKeyDataSource {
    suspend fun createKey(key: String, email: String, validFrom: String)
    suspend fun isValidKey(key: String): Boolean
    suspend fun getKeyByEmail(email: String): ApiKey?

}
