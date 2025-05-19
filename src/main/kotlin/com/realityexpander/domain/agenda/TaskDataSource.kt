package com.realityexpander.domain.agenda

import com.realityexpander.data.models.Task

interface TaskDataSource {
    suspend fun insertTask(task: Task): Boolean
    suspend fun deleteTask(taskId: String, ownerId: String): Boolean
    suspend fun getTaskById(taskId: String): Task?
    suspend fun updateTaskById(taskId: String, task: Task): Boolean
}