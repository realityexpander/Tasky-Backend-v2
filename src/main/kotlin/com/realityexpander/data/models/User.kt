package com.realityexpander.data.models

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.Instant

@Suppress("PropertyName") // _id is a reserved property name in MongoDB
data class User(
    val email: String,
    val fullName: String,
    val hashedPassword: String,
    val salt: String,
    val refreshToken: String,
    val createdAt: String = Instant.now().toString(),
    @BsonId val _id: String = ObjectId().toHexString()
)
