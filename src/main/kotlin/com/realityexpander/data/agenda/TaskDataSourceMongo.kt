package com.realityexpander.data.agenda

import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.realityexpander.data.models.Task
import com.realityexpander.domain.agenda.TaskDataSource
import kotlinx.coroutines.flow.firstOrNull

class TaskDataSourceMongo(
    db: MongoDatabase
): TaskDataSource {

    private val tasks = db.getCollection("task", Task::class.java)

    override suspend fun insertTask(task: Task): Boolean {
        return tasks.insertOne(task).wasAcknowledged()
    }

    override suspend fun deleteTask(taskId: String, ownerId: String): Boolean {
        return tasks.deleteOne(
            and(
                eq("_id", taskId),
                eq("userId", ownerId)
            )
        ).deletedCount > 0
    }

    override suspend fun getTaskById(taskId: String): Task? {
        return tasks.find(eq("_id", taskId)).firstOrNull()
    }

    override suspend fun updateTaskById(taskId: String, task: Task): Boolean {
        return tasks.replaceOne(eq("_id", taskId), task).wasAcknowledged()
    }
}