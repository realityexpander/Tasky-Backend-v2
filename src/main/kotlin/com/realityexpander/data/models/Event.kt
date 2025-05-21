package com.realityexpander.data.models

import com.amazonaws.services.s3.AmazonS3
import com.realityexpander.routing.responses.PhotoDTO
import com.realityexpander.sdk.AWS_BUCKET_NAME
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.Instant
import java.util.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

@Serializable
@Suppress("PropertyName") // _id is a reserved property name in MongoDB
data class Event(
    val title: String,
    val description: String?,
    val from: Long,
    val to: Long,
    val host: String,
    val photoKeys: List<String>,
    val attendeeIds: List<String>,
    val createdAt: String = Instant.now().toString(),
    @BsonId
    val _id: String = ObjectId.get().toHexString()
) {
    fun containsUser(userId: String) = attendeeIds.contains(userId) || host == userId

    suspend fun getPhotos(s3: AmazonS3, ioDispatcher: CoroutineDispatcher): List<PhotoDTO> {
        return withContext(ioDispatcher) {
            photoKeys.map { key ->
                async {
                    PhotoDTO(
                        key = key,
                        url = s3.generatePresignedUrl(
                            AWS_BUCKET_NAME,
                            key,
                            Date(System.currentTimeMillis() + 6.days.inWholeMilliseconds),
                        ).toExternalForm()
                    )
                }
            }.map { it.await() }
        }
    }
}
