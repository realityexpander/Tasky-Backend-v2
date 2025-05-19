package com.realityexpander.data.models

import org.bson.codecs.pojo.annotations.BsonId
import java.time.Instant

data class KilledToken(
    @BsonId
    val token: String, // will be named "_id" in MongoDB
    val createdAt: String = Instant.now().toString(),
)
