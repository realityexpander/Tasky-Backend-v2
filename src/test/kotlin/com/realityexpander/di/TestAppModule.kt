package com.realityexpander.di

import com.amazonaws.services.s3.AmazonS3
import com.realityexpander.data.hashing.SHA256HashingService
import com.realityexpander.data.token.JwtTokenService
import com.realityexpander.domain.agenda.*
import com.realityexpander.domain.security.auth.ApiKeyDataSource
import com.realityexpander.domain.security.hashing.HashingService
import com.realityexpander.domain.security.token.TokenService
import com.realityexpander.domain.user.UserDataSource
import com.realityexpander.domain.user.UserDataValidationService
import com.realityexpander.domain.util.DispatcherProvider
import com.realityexpander.domain.util.EnvironmentProvider
import com.realityexpander.fakes.*
import com.realityexpander.util.TestDispatcherProvider
import io.mockk.every
import io.mockk.mockk
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import java.net.URI

val testAppModule = module {
    singleOf(::UserDataSourceFake).bind<UserDataSource>()
    singleOf(::KilledTokenDataSourceFake).bind<KilledTokenDataSource>()
    singleOf(::ApiKeyDataSourceFake).bind<ApiKeyDataSource>()
    singleOf(::AgendaDataSourceFake).bind<AgendaDataSource>()
    singleOf(::EventDataSourceFake).bind<EventDataSource>()
    singleOf(::TaskDataSourceFake).bind<TaskDataSource>()
    singleOf(::ReminderDataSourceFake).bind<ReminderDataSource>()
    singleOf(::AttendeeDataSourceFake).bind<AttendeeDataSource>()
    singleOf(::SHA256HashingService).bind<HashingService>()
    singleOf(::JwtTokenService).bind<TokenService>()
    singleOf(::UserDataValidationService).bind()
    single<AmazonS3> {
        mockk(relaxed = true) {
            every {
                generatePresignedUrl(
                    any(),
                    any(),
                    any())
            } returns URI.create("https://test.com").toURL()
        }
    }

    single { TestEnvironmentProvider }.bind<EnvironmentProvider>()
    single { TestDispatcherProvider() }.bind<DispatcherProvider>()
}