package com.realityexpander.fakes

import com.realityexpander.data.models.Agenda
import com.realityexpander.domain.agenda.AgendaDataSource
import java.time.LocalDate
import java.time.ZoneId

class AgendaDataSourceFake(
    private val eventDataSourceFake: EventDataSourceFake,
    private val taskDataSourceFake: TaskDataSourceFake,
    private val reminderDataSourceFake: ReminderDataSourceFake,
): AgendaDataSource {

    override suspend fun getAgenda(userId: String, date: LocalDate): Agenda {
        val fromTimestamp = date.atStartOfDay(ZoneId.of("UTC")).toEpochSecond() * 1000
        val toTimestamp = date.plusDays(1).atStartOfDay(ZoneId.of("UTC")).toEpochSecond() * 1000
        val events = eventDataSourceFake.events.filter {
            it.from >= fromTimestamp && it.to < toTimestamp &&
                    (it.host == userId || userId in it.attendeeIds)
        }
        val tasks = taskDataSourceFake.tasks.filter {
            it.time in fromTimestamp until toTimestamp && it.userId == userId
        }
        val reminders = reminderDataSourceFake.reminders.filter {
            it.time in fromTimestamp until toTimestamp && it.userId == userId
        }
        return Agenda(
            events = events,
            tasks = tasks,
            reminders = reminders
        )
    }

    override suspend fun getFullAgenda(userId: String): Agenda {
        val events = eventDataSourceFake.events.filter { it.host == userId }
        val tasks = taskDataSourceFake.tasks.filter { it.userId == userId }
        val reminders = reminderDataSourceFake.reminders.filter { it.userId == userId }

        return Agenda(
            events = events,
            tasks = tasks,
            reminders = reminders
        )
    }
}