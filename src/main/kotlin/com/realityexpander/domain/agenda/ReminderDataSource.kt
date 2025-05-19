package com.realityexpander.domain.agenda

import com.realityexpander.data.models.Reminder

interface ReminderDataSource {
    suspend fun insertReminder(reminder: Reminder): Boolean
    suspend fun deleteReminder(reminderId: String, ownerId: String): Boolean
    suspend fun getReminderById(reminderId: String): Reminder?
    suspend fun updateReminderById(reminderId: String, reminder: Reminder): Boolean
}