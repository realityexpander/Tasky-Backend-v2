package com.realityexpander.data.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.Instant

@Serializable
@Suppress("PropertyName") // _id is a reserved property name in MongoDB
data class Reminder(
    val title: String,
    val description: String?,
    val userId: String,
    val time: Long,
    val remindAt: Long,
    val createdAt: String = Instant.now().toString(),
    @BsonId
    val _id: String = ObjectId.get().toHexString()
)
