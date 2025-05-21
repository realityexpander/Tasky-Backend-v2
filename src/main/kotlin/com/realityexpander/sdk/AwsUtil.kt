package com.realityexpander.sdk

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.*
import kotlin.time.Duration.Companion.days

const val AWS_BUCKET_NAME = "tasky"

suspend fun AmazonS3.putS3Object(bucketName: String, objectKey: String, inputStream: InputStream): PutObjectResult {
    return withContext(Dispatchers.IO) {
        val request = PutObjectRequest(
            bucketName,
            objectKey,
            inputStream,
            ObjectMetadata().apply {
                contentLength = inputStream.available().toLong()
            }
        )

        putObject(request)
    }
}


suspend fun AmazonS3.deleteBucketObjects(bucketName: String, keys: List<String>, ioDispatcher: CoroutineDispatcher): DeleteObjectsResult? {
    if(keys.isEmpty()) {
        return null
    }
    return withContext(ioDispatcher) {
        val request = DeleteObjectsRequest(bucketName)
            .withKeys(*keys.toTypedArray())

        deleteObjects(request)
    }
}

suspend fun AmazonS3.getS3Url(key: String): String {
    return withContext(Dispatchers.IO) {
        generatePresignedUrl(AWS_BUCKET_NAME,
            key,
            Date(6.days.inWholeMilliseconds)).toExternalForm()
    }
}
