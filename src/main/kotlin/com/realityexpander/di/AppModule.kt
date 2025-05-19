package com.realityexpander.di

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.realityexpander.data.agenda.*
import com.realityexpander.data.auth.ApiKeyDataSourceMongo
import com.realityexpander.data.auth.KilledTokenDataSourceMongo
import com.realityexpander.data.cleanup.DatabaseCleanupService
import com.realityexpander.data.hashing.SHA256HashingService
import com.realityexpander.data.token.JwtTokenService
import com.realityexpander.data.user.UserDataSourceMongo
import com.realityexpander.domain.agenda.*
import com.realityexpander.domain.cleanup.CleanupService
import com.realityexpander.domain.security.auth.ApiKeyDataSource
import com.realityexpander.domain.security.hashing.HashingService
import com.realityexpander.domain.security.token.TokenService
import com.realityexpander.domain.user.UserDataSource
import com.realityexpander.domain.user.UserDataValidationService
import com.realityexpander.domain.util.DevelopmentEnvironmentProvider
import com.realityexpander.domain.util.DispatcherProvider
import com.realityexpander.domain.util.EnvironmentProvider
import com.realityexpander.domain.util.StandardDispatcherProvider
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    val dbName = "tasky"

    single {
        val environmentVariables = get<EnvironmentProvider>()
        val mongoPassword = environmentVariables.mongoPw
        val mongoUser = environmentVariables.mongoUser
        val mongoConnectionStringPrefix = environmentVariables.mongoConnectionStringPrefix
        val mongoConnectionString = environmentVariables.mongoConnectionString

        MongoClient.create(
            connectionString = "$mongoConnectionStringPrefix$mongoUser:$mongoPassword$mongoConnectionString/$dbName?retryWrites=true&w=majority"
        )
    }
    single {
        val client = get<MongoClient>()
        client.getDatabase(dbName)
    }
    single<UserDataSource> {
        UserDataSourceMongo(get())
    }
    single<KilledTokenDataSource> {
        KilledTokenDataSourceMongo(get())
    }
    single<ApiKeyDataSource> {
        ApiKeyDataSourceMongo(get())
    }
    single<AgendaDataSource> {
        AgendaDataSourceMongo(get())
    }
    single<EventDataSource> {
        EventDataSourceMongo(get())
    }
    single<TaskDataSource> {
        TaskDataSourceMongo(get())
    }
    single<ReminderDataSource> {
        ReminderDataSourceMongo(get())
    }
    single<AttendeeDataSource> {
        AttendeeDataSourceMongo(get())
    }
    single<HashingService> {
        SHA256HashingService()
    }
    single<TokenService> {
        JwtTokenService()
    }
    single {
        UserDataValidationService()
    }
    single<AmazonS3> {
        val environmentVariables = get<EnvironmentProvider>()

        // Using AWS S3 instance
//        AmazonS3ClientBuilder.standard()
//            .withCredentials(
//                AWSStaticCredentialsProvider(
//                    object : AWSCredentials {
//                        override fun getAWSAccessKeyId(): String {
//                            return environmentVariables.s3AccessKeyId
//                        }
//
//                        override fun getAWSSecretKey(): String {
//                            return environmentVariables.s3SecretAccessKey
//                        }
//                    }
//                )
//            )
//            .withRegion(environmentVariables.s3SigningRegion)
//            .build()

        // Using IDriveE2 S3 instance
        AmazonS3ClientBuilder.standard()
            .withCredentials(
                AWSStaticCredentialsProvider(
                    object : AWSCredentials {
                        override fun getAWSAccessKeyId(): String {
                            return environmentVariables.s3AccessKeyId
                        }

                        override fun getAWSSecretKey(): String {
                            return environmentVariables.s3SecretAccessKey
                        }
                    }
                )
            )
            .withEndpointConfiguration( // IDriveE2 S3 requires configuring the endpoint
                com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration(
                    environmentVariables.s3Endpoint,
                    environmentVariables.s3SigningRegion
                )
            )
            .build()
    }

    singleOf(::DatabaseCleanupService).bind<CleanupService>()

    single { DevelopmentEnvironmentProvider }.bind<EnvironmentProvider>()

    single { StandardDispatcherProvider }.bind<DispatcherProvider>()
}
