package com.realityexpander.domain.user

import com.realityexpander.data.models.User

interface UserDataSource {
    suspend fun insertUser(user: User): Boolean
    suspend fun getUserById(id: String): User?
    suspend fun getUserByEmail(email: String): User?
    suspend fun checkRefreshTokenForUser(userId: String, refreshToken: String): Boolean
}