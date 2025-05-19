package com.realityexpander.data.models

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Suppress("PropertyName") // _id is a reserved property name in MongoDB
data class ApiKey(
    val key: String,
    val expiresAt: String,
    val email: String,
    val validFrom: String,
    @BsonId val _id: ObjectId = ObjectId()
)
