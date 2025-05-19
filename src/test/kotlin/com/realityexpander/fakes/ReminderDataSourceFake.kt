package com.realityexpander.fakes

import com.realityexpander.data.models.Reminder
import com.realityexpander.domain.agenda.ReminderDataSource

class ReminderDataSourceFake: ReminderDataSource {

    val reminders = mutableListOf<Reminder>()
    
    override suspend fun insertReminder(reminder: Reminder): Boolean {
        return reminders.add(reminder)
    }

    override suspend fun deleteReminder(reminderId: String, ownerId: String): Boolean {
        return reminders.removeIf { it._id == reminderId && it.userId == ownerId }
    }

    override suspend fun getReminderById(reminderId: String): Reminder? {
        return reminders.find { it._id == reminderId }
    }

    override suspend fun updateReminderById(reminderId: String, reminder: Reminder): Boolean {
        val existingReminder = reminders.find { it._id == reminderId }
        val index = reminders.indexOf(existingReminder)
        reminders[index] = reminder

        return true
    }
}