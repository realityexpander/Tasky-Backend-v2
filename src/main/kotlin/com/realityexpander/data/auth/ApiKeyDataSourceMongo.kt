package com.realityexpander.data.auth

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.FindOneAndReplaceOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.realityexpander.data.models.ApiKey
import com.realityexpander.domain.security.auth.ApiKeyDataSource
import kotlinx.coroutines.flow.firstOrNull
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class ApiKeyDataSourceMongo(
    db: MongoDatabase
): ApiKeyDataSource {

    private val keys = db.getCollection("apiKey", ApiKey::class.java)

    override suspend fun isValidKey(key: String): Boolean {
        val keyInDb = keys.find(eq("key", key)).firstOrNull() ?: return false
        val keyExpirationDate = Instant.parse(keyInDb.expiresAt)
        val keyValidFrom = Instant.parse(keyInDb.validFrom)
        val nowUtc = Instant.now()

        return keyValidFrom <= nowUtc && nowUtc <= keyExpirationDate
    }

    override suspend fun getKeyByEmail(email: String): ApiKey? {
        return keys.find(eq("email", email)).firstOrNull()
    }

    override suspend fun createKey(key: String, email: String, validFrom: String) {
        val nowUtc = ZonedDateTime.now(ZoneId.of("UTC"))
        val expiresAt = nowUtc.plusMonths(12L).toInstant().toString()
        val existingKey = keys.find(eq("email", email)).firstOrNull()

        if(existingKey == null) {
            keys.insertOne(
                ApiKey(
                    key = key,
                    validFrom = validFrom,
                    expiresAt = expiresAt,
                    email = email,
                )
            )
        } else {
            keys.findOneAndReplace(
                eq("email", email),
                ApiKey(
                    _id = existingKey._id,
                    key = key,
                    validFrom = validFrom,
                    expiresAt = expiresAt,
                    email = email,
                ),
                FindOneAndReplaceOptions()
                    .upsert(true)
                    .returnDocument(ReturnDocument.AFTER)
            )
        }
    }
}
