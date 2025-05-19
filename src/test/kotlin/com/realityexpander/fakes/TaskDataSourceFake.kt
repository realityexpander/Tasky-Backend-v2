package com.realityexpander.fakes

import com.realityexpander.data.models.Task
import com.realityexpander.domain.agenda.TaskDataSource

class TaskDataSourceFake: TaskDataSource {

    val tasks = mutableListOf<Task>()
    
    override suspend fun insertTask(task: Task): Boolean {
        return tasks.add(task)
    }

    override suspend fun deleteTask(taskId: String, ownerId: String): Boolean {
        return tasks.removeIf { it._id == taskId && it.userId == ownerId }
    }

    override suspend fun getTaskById(taskId: String): Task? {
        return tasks.find { it._id == taskId }
    }

    override suspend fun updateTaskById(taskId: String, task: Task): Boolean {
        val existingTask = tasks.find { it._id == taskId }
        val index = tasks.indexOf(existingTask)
        tasks[index] = task

        return true
    }
}