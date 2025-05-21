package com.realityexpander.data.user

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Filters.eq
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.realityexpander.data.models.User
import com.realityexpander.domain.user.UserDataSource
import kotlinx.coroutines.flow.firstOrNull

class UserDataSourceMongo(
    db: MongoDatabase
): UserDataSource {

    private val users = db.getCollection("user", User::class.java)

    override suspend fun insertUser(user: User): Boolean {
        return users.insertOne(user).wasAcknowledged()
    }

    override suspend fun getUserById(id: String): User? {
        return users.find(eq("_id", id)).firstOrNull()
    }

    override suspend fun getUserByEmail(email: String): User? {
        return users.find(eq("email", email)).firstOrNull()
    }

    override suspend fun checkRefreshTokenForUser(userId: String, refreshToken: String): Boolean {
        return users.find(
            Filters.and(
                eq("_id", userId),
                eq("refreshToken", refreshToken),
            )
        ).firstOrNull() != null
    }
}
