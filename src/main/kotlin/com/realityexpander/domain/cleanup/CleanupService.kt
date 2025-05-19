package com.realityexpander.domain.cleanup

interface CleanupService {
    suspend fun cleanupOldEntries(deleteBefore: String): Long
}