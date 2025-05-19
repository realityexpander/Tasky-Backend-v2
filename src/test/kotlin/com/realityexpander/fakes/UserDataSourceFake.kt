package com.realityexpander.fakes

import com.realityexpander.data.models.User
import com.realityexpander.domain.user.UserDataSource

class UserDataSourceFake: UserDataSource {

    val users = mutableListOf<User>()

    override suspend fun insertUser(user: User): Boolean {
        return users.add(user)
    }

    override suspend fun getUserById(id: String): User? {
        return users.find { it._id == id }
    }

    override suspend fun getUserByEmail(email: String): User? {
        return users.find { it.email == email }
    }

    override suspend fun checkRefreshTokenForUser(userId: String, refreshToken: String): Boolean {
        return users.any {
            it._id == userId && it.refreshToken == refreshToken
        }
    }
}