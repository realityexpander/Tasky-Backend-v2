package com.realityexpander.data.agenda

import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.realityexpander.data.models.Reminder
import com.realityexpander.domain.agenda.ReminderDataSource
import kotlinx.coroutines.flow.firstOrNull

class ReminderDataSourceMongo(
    db: MongoDatabase
): ReminderDataSource {

    private val reminders = db.getCollection("reminder", Reminder::class.java)

    override suspend fun insertReminder(reminder: Reminder): Boolean {
        return reminders.insertOne(reminder)
            .wasAcknowledged()
    }

    override suspend fun deleteReminder(reminderId: String, ownerId: String): Boolean {
        return reminders.deleteOne(and(
            eq("_id", reminderId),
            eq("userId", ownerId)
        )).deletedCount > 0
    }

    override suspend fun getReminderById(reminderId: String): Reminder? {
        return reminders.find(eq("_id", reminderId))
            .firstOrNull()
    }

    override suspend fun updateReminderById(reminderId: String, reminder: Reminder): Boolean {
        return reminders.replaceOne(
            eq("_id", reminderId),
            reminder
        ).wasAcknowledged()
    }
}