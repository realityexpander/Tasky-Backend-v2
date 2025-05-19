package com.realityexpander.routing

import com.realityexpander.data.models.Task
import com.realityexpander.domain.agenda.TaskDataSource
import com.realityexpander.domain.util.ErrorMessage
import com.realityexpander.inject
import com.realityexpander.routing.principal.userId
import com.realityexpander.routing.requests.CreateTaskRequest
import com.realityexpander.routing.requests.UpdateTaskRequest
import com.realityexpander.routing.responses.TaskDTOResponse
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.task() {
    val taskDataSource: TaskDataSource by inject()

    authenticate("jwt") {
        post("task") {
            val request = call.receiveNullable<CreateTaskRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val task = Task(
                title = request.title,
                description = request.description,
                isDone = false,
                userId = call.userId,
                remindAt = request.remindAt,
                time = request.time,
                _id = request.id
            )
            val wasAcknowledged = taskDataSource.insertTask(task)
            if(!wasAcknowledged) {
                call.respond(HttpStatusCode.Conflict)
                return@post
            }

            call.respond(HttpStatusCode.OK)
        }

        get("task") {
            val taskId = call.parameters["taskId"]
            if(taskId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorMessage(
                        message = "No task ID provided"
                    )
                )
                return@get
            }

            val task = taskDataSource.getTaskById(taskId)
            if(task == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorMessage(
                        message = "Task not found"
                    )
                )
                return@get
            }

            if(task.userId != call.userId) {
                call.respond(HttpStatusCode.Forbidden)
                return@get
            }

            call.respond(
                HttpStatusCode.OK,
                TaskDTOResponse(
                    title = task.title,
                    description = task.description,
                    time = task.time,
                    remindAt = task.remindAt,
                    id = task._id.toString(),
                    isDone = task.isDone
                )
            )
        }

        delete("task") {
            val taskId = call.parameters["taskId"]
            if(taskId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorMessage(
                        message = "No task ID provided"
                    )
                )
                return@delete
            }

            val wasAcknowledged = taskDataSource.deleteTask(taskId, call.userId)
            if(!wasAcknowledged) {
                call.respond(HttpStatusCode.Conflict)
                return@delete
            }

            call.respond(HttpStatusCode.OK)
        }

        put("task") {
            val request = call.receiveNullable<UpdateTaskRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }

            val taskFromDb = taskDataSource.getTaskById(request.id)
            if(taskFromDb == null) {
                call.respond(
                    HttpStatusCode.Conflict,
                    ErrorMessage(message = "Task not found")
                )
                return@put
            }

            if(taskFromDb.userId != call.userId) {
                call.respond(HttpStatusCode.Forbidden)
                return@put
            }

            val task = taskFromDb.copy(
                title = request.title,
                description = request.description,
                _id = request.id,
                isDone = request.isDone,
                remindAt = request.remindAt,
                time = request.time,
            )
            val wasAcknowledged = taskDataSource.updateTaskById(request.id, task)
            if(!wasAcknowledged) {
                call.respond(HttpStatusCode.Conflict)
                return@put
            }

            call.respond(HttpStatusCode.OK)
        }
    }
}
