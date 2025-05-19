package com.realityexpander.data.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.Instant

/**
 * Task data class representing a task in the system.
 *
 * @property title The title of the task.
 * @property description The description of the task (optional).
 * @property isDone Indicates whether the task is completed or not.
 * @property userId The ID of the user associated with the task.
 * @property time The time associated with the task.
 * @property remindAt The time to remind about the task.
 * @property createdAt The creation timestamp of the task (default is current time).
 * @property _id The unique identifier for the task (default is a new ObjectId).
 */
@Serializable
@Suppress("PropertyName") // _id is a reserved property name in MongoDB
data class Task(
    val title: String,
    val description: String?,
    val isDone: Boolean,
    val userId: String,
    val time: Long,
    val remindAt: Long,
    val createdAt: String = Instant.now().toString(),
    @BsonId
    val _id: String = ObjectId.get().toHexString()
)
