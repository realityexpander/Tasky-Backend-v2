package com.realityexpander.util

import com.realityexpander.data.hashing.SHA256HashingService
import com.realityexpander.data.models.*
import com.realityexpander.domain.agenda.*
import com.realityexpander.domain.user.UserDataSource
import com.realityexpander.fakes.*
import org.koin.java.KoinJavaComponent.inject
import java.time.Instant
import java.time.ZonedDateTime

val testUsers = (1..10).map {
    val password = "Test12345"
    val hashedPassword = SHA256HashingService().generateSaltedHash(password)
    User(
        _id = "user$it",
        email = "test$it@test.com",
        fullName = "First$it Last$it",
        hashedPassword = hashedPassword.hash,
        salt = hashedPassword.salt,
        refreshToken = "refresh"
    )
}

val testApiKeys = testUsers.map {
    ApiKey(
        key = it._id,
        expiresAt = ZonedDateTime.now().plusMonths(12L).toInstant().toString(),
        validFrom = ZonedDateTime.now().minusMonths(12L).toInstant().toString(),
        email = it.email
    )
}

val testEvents = listOf(
    Event(
        title = "Event 1",
        description = "Description 1",
        from = 1700159590000,
        to = 1700159950000,
        host = "user1",
        photoKeys = emptyList(),
        attendeeIds = listOf(
            "user1",
            "user2",
            "user3",
        )
    ),
    Event(
        title = "Event 2",
        description = "Description 2",
        from = 1699427501885,
        to = 1699513901886,
        host = "user1",
        photoKeys = emptyList(),
        attendeeIds = listOf(
            "user3",
            "user4",
            "user5",
        )
    ),
    Event(
        title = "Event 3",
        description = "Description 3",
        from = 1714331040000,
        to = 1714332840000,
        host = "user2",
        photoKeys = emptyList(),
        attendeeIds = listOf(
            "user5",
            "user6",
            "user7",
        )
    ),
    Event(
        title = "Event 4",
        description = "Description 4",
        from = 1714331040000,
        to = 1714332840000,
        host = "user3",
        photoKeys = emptyList(),
        attendeeIds = listOf("user3")
    ),
    Event(
        title = "Event 5",
        description = "Description 5",
        from = 1714331040000,
        to = 1714332840000,
        host = "user4",
        photoKeys = emptyList(),
        attendeeIds = listOf("user4")
    ),
)

val testTasks = listOf(
    Task(
        title = "Task 1",
        description = "Description 1",
        isDone = false,
        userId = "user1",
        time = 1700159900000,
        remindAt = 1677054600000,
        createdAt = Instant.ofEpochMilli(1677052824838).toString()
    ),
    Task(
        title = "Task 2",
        description = "Description 2",
        isDone = true,
        userId = "user1",
        time = 1679008682354,
        remindAt = 1679008082354,
        createdAt = Instant.ofEpochMilli(1679008683345).toString()
    ),
    Task(
        title = "Task 3",
        description = "Description 3",
        isDone = false,
        userId = "user2",
        time = 1680622250000,
        remindAt = 1680618600000,
        createdAt = Instant.ofEpochMilli(1680624488885).toString()
    ),
    Task(
        title = "Task 4",
        description = "Description 4",
        isDone = true,
        userId = "user3",
        time = 1700159400000,
        remindAt = 1700158800000,
        createdAt = Instant.ofEpochMilli(1700065815327).toString()
    ),
)

val testReminders = listOf(
    Reminder(
        title = "Reminder 1",
        description = "Description 1",
        userId = "user1",
        time = 1700159500000,
        remindAt = 1700158800000
    ),
    Reminder(
        title = "Reminder 2",
        description = "Description 2",
        userId = "user2",
        time = 1700159500000,
        remindAt = 1700158800000
    ),
    Reminder(
        title = "Reminder 3",
        description = "Description 3",
        userId = "user3",
        time = 1700159500000,
        remindAt = 1700158800000
    ),
)

suspend fun setupFakeDataSources() {
    val attendeeDataSource by inject<AttendeeDataSource>(AttendeeDataSourceFake::class.java)
    val eventDataSource by inject<EventDataSource>(EventDataSourceFake::class.java)
    val taskDataSource by inject<TaskDataSource>(TaskDataSourceFake::class.java)
    val reminderDataSource by inject<ReminderDataSource>(ReminderDataSourceFake::class.java)
    val userDataSource by inject<UserDataSource>(UserDataSourceFake::class.java)
    val apiKeyDataSource by inject<ApiKeyDataSourceFake>(ApiKeyDataSourceFake::class.java)

    testUsers.forEach {
        userDataSource.insertUser(it)
    }

    testEvents.forEach {
        eventDataSource.insertEvent(it)
        attendeeDataSource.createAttendeesForEvent(
            attendeeIds = it.attendeeIds,
            eventId = it._id,
            remindAtForOthers = System.currentTimeMillis(),
            remindAtForOwner = System.currentTimeMillis(),
            eventOwnerId = it.host
        )
    }

    testTasks.forEach {
        taskDataSource.insertTask(it)
    }

    testReminders.forEach {
        reminderDataSource.insertReminder(it)
    }

    testApiKeys.forEach {
        apiKeyDataSource.createKey(it.key, it.email, it.validFrom)
    }
}

fun cleanUpDataSources() {
    val attendeeDataSource by inject<AttendeeDataSourceFake>(AttendeeDataSourceFake::class.java)
    val eventDataSource by inject<EventDataSourceFake>(EventDataSourceFake::class.java)
    val taskDataSource by inject<TaskDataSourceFake>(TaskDataSourceFake::class.java)
    val reminderDataSource by inject<ReminderDataSourceFake>(ReminderDataSourceFake::class.java)
    val userDataSource by inject<UserDataSourceFake>(UserDataSourceFake::class.java)
    val killedTokenDataSource by inject<KilledTokenDataSourceFake>(KilledTokenDataSourceFake::class.java)
    val apiKeyDataSource by inject<ApiKeyDataSourceFake>(ApiKeyDataSourceFake::class.java)

    attendeeDataSource.attendees.clear()
    eventDataSource.events.clear()
    taskDataSource.tasks.clear()
    reminderDataSource.reminders.clear()
    userDataSource.users.clear()
    killedTokenDataSource.killedTokens.clear()
    apiKeyDataSource.apiKeys.clear()
}