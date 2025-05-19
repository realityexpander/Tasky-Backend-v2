package com.realityexpander.routing.util

import io.ktor.http.content.*
import io.ktor.utils.io.readBuffer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.io.readByteArray
import java.io.ByteArrayInputStream
import java.io.InputStream

const val MAX_FILE_SIZE = 1000000
class PayloadTooLargeException: Exception("The file exceeds the maximum allowed size of $MAX_FILE_SIZE")

suspend fun PartData.FileItem.saveOld(ioDispatcher: CoroutineDispatcher): InputStream {
    return withContext(ioDispatcher) {
        val bytes = streamProvider().readBytes()

        if(bytes.size > MAX_FILE_SIZE) {
            throw PayloadTooLargeException()
        }

        ByteArrayInputStream(bytes)
    }
}

suspend fun PartData.FileItem.save(ioDispatcher: CoroutineDispatcher): InputStream {
    return withContext(ioDispatcher) {
        // check size
        val fileSize = this@save.provider().readBuffer().size
        if (fileSize > MAX_FILE_SIZE) {
            throw PayloadTooLargeException()
        }

        val byteArray = provider().readBuffer().readByteArray()
        if(byteArray.size > MAX_FILE_SIZE) {
            throw PayloadTooLargeException()
        }

        ByteArrayInputStream(byteArray)
    }
}
