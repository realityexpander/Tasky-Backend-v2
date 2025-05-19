package com.realityexpander.data.auth

import com.mongodb.client.model.Filters.eq
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.realityexpander.data.models.KilledToken
import com.realityexpander.domain.agenda.KilledTokenDataSource
import kotlinx.coroutines.flow.firstOrNull

class KilledTokenDataSourceMongo(db: MongoDatabase): KilledTokenDataSource {

    private val killedTokens = db.getCollection("killedToken", KilledToken::class.java)

    override suspend fun insertKilledToken(token: String): Boolean {
        return killedTokens.insertOne(KilledToken(token)).wasAcknowledged()
    }

    override suspend fun isKilled(token: String): Boolean {
        return killedTokens.find(eq("token", token)).firstOrNull() != null
    }
}