package com.realityexpander.sdk

import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.*
import kotlin.time.Duration.Companion.days

const val S3_BUCKET_NAME = "tasky"

suspend fun AmazonS3.putS3Object(bucketName: String, objectKey: String, inputStream: InputStream): PutObjectResult {
	return withContext(Dispatchers.IO) {
		val request = PutObjectRequest(
			bucketName,
			objectKey,
			inputStream,
			ObjectMetadata().apply {
				contentLength = inputStream.available().toLong()
				contentType = "image/jpeg"
			}
		).withInputStream(inputStream)

		putObject(request)
	}
}


suspend fun AmazonS3.deleteBucketObjects(
	bucketName: String,
	keys: List<String>,
	ioDispatcher: CoroutineDispatcher
): DeleteObjectsResult? {
	if (keys.isEmpty()) {
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
		val request = generatePresignedUrlRequest(key)
		generatePresignedUrl(request)
			.toExternalForm()
	}
}

fun AmazonS3.generatePresignedUrlRequest(
	key: String,
	bucketName: String = S3_BUCKET_NAME,
	expiration: Date = Date(System.currentTimeMillis() + 6.days.inWholeMilliseconds)
): GeneratePresignedUrlRequest {
	val request = GeneratePresignedUrlRequest(bucketName, key)
		.withMethod(HttpMethod.GET)
		.withExpiration(expiration)
	request.addRequestParameter("Content-Type", "image/jpeg")

	// leave for reference
//	request.requestParameters["ContentSha256"] = "UNSIGNED-PAYLOAD"
//	request.addRequestParameter("x-amz-content-sha256", "UNSIGNED-PAYLOAD")

	return request
}