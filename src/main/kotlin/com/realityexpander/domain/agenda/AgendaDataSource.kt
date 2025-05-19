package com.realityexpander.domain.agenda

import com.realityexpander.data.models.Agenda
import java.time.LocalDate

interface AgendaDataSource {
    suspend fun getAgenda(userId: String, date: LocalDate): Agenda
    suspend fun getFullAgenda(userId: String): Agenda
}