package com.realityexpander.data.cleanup

import com.amazonaws.services.s3.AmazonS3
//import software.amazon.awssdk.services.s3.model.DeleteObjectRequest  // AWS SDK v2
import com.mongodb.client.model.Filters.lt
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.realityexpander.data.models.*
import com.realityexpander.domain.cleanup.CleanupService
import com.realityexpander.sdk.S3_BUCKET_NAME
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.supervisorScope

class DatabaseCleanupService(
    db: MongoDatabase,
    private val s3: AmazonS3
): CleanupService {

    // Breaking changes in AWS SDK v2
//    val s32 = S3Client.create()
//    fun zap() {
//         s32.deleteObject { delete ->
//               delete.bucket(AWS_BUCKET_NAME)
//                  .key("test")
//         }
//        s32.deleteObject(DeleteObjectRequest.builder()
//            .bucket(AWS_BUCKET_NAME)
//            .key("test")
//            .build()
//        )
//    }

    private val killedTokens = db.getCollection<KilledToken>("killedtoken")
    private val users = db.getCollection<User>("user")
    private val events = db.getCollection<Event>("event")
    private val tasks = db.getCollection<Task>("task")
    private val reminders = db.getCollection<Reminder>("reminder")
    private val attendees = db.getCollection<Attendee>("attendee")

    // Remove all entries older than `deleteBefore` timestamp
    // `deleteBefore` is a timestamp in the format: yyyy-MM-dd'T'HH:mm:ss.SSSSS'Z' (2025-05-16T22:03:49.421009Z)
    @Suppress("SpellCheckingInspection")
    override suspend fun cleanupOldEntries(deleteBefore: String): Long {
        println("CleanupService: deleteBefore = $deleteBefore")

        return supervisorScope {
            val eventsToDelete = events.find<Event>(
                lt("createdAt",  deleteBefore)
            ).toList()

            eventsToDelete.size.toLong()

            // Delete photos from S3
            eventsToDelete.forEach {
                it.photoKeys.forEach { key ->
                    s3.deleteObject(S3_BUCKET_NAME, key)
                }
            }

            // Delete all entries older than `deleteBefore` timestamp
            listOf(
                killedTokens,
                users,
                events,
                tasks,
                reminders,
                attendees
            ).fold(initial = listOf<Deferred<Long>>()) { deferred, collection ->
                deferred + async {
                    collection.deleteMany(
                        lt("createdAt", deleteBefore)
                    ).deletedCount
                }
            }.sumOf {
                it.await()
            }
        }
    }
}