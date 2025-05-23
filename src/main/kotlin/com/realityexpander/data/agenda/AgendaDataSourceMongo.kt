package com.realityexpander.data.agenda

import com.mongodb.client.model.Filters.*
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.realityexpander.data.models.Agenda
import com.realityexpander.data.models.Event
import com.realityexpander.data.models.Reminder
import com.realityexpander.data.models.Task
import com.realityexpander.domain.agenda.AgendaDataSource
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.supervisorScope
import java.time.LocalDate
import java.time.ZoneId

class AgendaDataSourceMongo(
    db: MongoDatabase
) : AgendaDataSource {

    private val events = db.getCollection("event", Event::class.java)
    private val tasks = db.getCollection("task", Task::class.java)
    private val reminders = db.getCollection("reminder", Reminder::class.java)

    override suspend fun getAgenda(userId: String, date: LocalDate): Agenda {
        return coroutineScope {
            val fromTimestamp = date.atStartOfDay(ZoneId.of("UTC")).toEpochSecond() * 1000
            val toTimestamp = date.plusDays(1).atStartOfDay(ZoneId.of("UTC")).toEpochSecond() * 1000
            val events = async {
                events
                    .find(
                        and(
                            gte("from", fromTimestamp),
                            lt("to", toTimestamp),
                            or(
                                eq("attendeeIds", userId),
                                eq("host", userId)
                            )
                        ),
                    ).toList()
            }
            val tasks = async {
                tasks.find(
                    and(
                        gte("time", fromTimestamp),
                        lt("time", toTimestamp),
                        eq("userId", userId)
                    )
                ).toList()
            }
            val reminders = async {
                reminders.find(
                    and(
                        gte("time", fromTimestamp),
                        lt("time", toTimestamp),
                        eq("userId", userId)
                    )
                ).toList()
            }

            Agenda(
                events.await(),
                tasks.await(),
                reminders.await()
            )
        }
    }

    override suspend fun getFullAgenda(userId: String): Agenda {
        return supervisorScope {
            val events = async {
                events.find(
                    or(
                        eq("attendeeIds", userId),
                        eq("host", userId)
                    )
                ).toList()
            }
            val tasks = async {
                tasks.find(eq("userId", userId)).toList()
            }
            val reminders = async {
                reminders.find(eq("userId", userId)).toList()
            }

            Agenda(
                events = events.await(),
                tasks = tasks.await(),
                reminders = reminders.await()
            )
        }
    }
}