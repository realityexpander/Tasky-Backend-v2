package com. realityexpander.fakes

import com.realityexpander.data.models.KilledToken
import com.realityexpander.domain.agenda.KilledTokenDataSource

class KilledTokenDataSourceFake: KilledTokenDataSource {

    val killedTokens = mutableListOf<KilledToken>()

    override suspend fun insertKilledToken(token: String): Boolean {
        return killedTokens.add(KilledToken(token))
    }

    override suspend fun isKilled(token: String): Boolean {
        return killedTokens.any { it.token == token }
    }
}