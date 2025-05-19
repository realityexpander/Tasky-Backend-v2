package com.realityexpander.domain.util

import com.typesafe.config.ConfigFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import java.io.File
import java.lang.IllegalStateException

interface EnvironmentProvider {
	val mongoUser: String
	val mongoPw: String
	val mongoConnectionStringPrefix: String
	val mongoConnectionString: String
	val jwtSecret: String
	val jwtIssuer: String
	val jwtAudience: String
	val s3AccessKeyId: String
	val s3SecretAccessKey: String
	val createApiKeyUser: String
	val createApiKeyPassword: String
}

const val MONGO_USER = "MONGO_USER"
const val MONGO_PASSWORD = "MONGO_PASSWORD"
const val MONGO_CONNECTION_STRING_PREFIX = "MONGO_CONNECTION_STRING_PREFIX"
const val MONGO_CONNECTION_STRING = "MONGO_CONNECTION_STRING"
const val JWT_SECRET = "JWT_SECRET"
const val JWT_ISSUER = "JWT_ISSUER"
const val JWT_AUDIENCE = "JWT_AUDIENCE"
const val S3_ACCESS_KEY_ID = "S3_ACCESS_KEY_ID"
const val S3_SECRET_ACCESS_KEY = "S3_SECRET_ACCESS_KEY"
const val CREATE_API_KEY_USER = "CREATE_API_KEY_USER"
const val CREATE_API_KEY_PASSWORD = "CREATE_API_KEY_PASSWORD"

@Serializable
data class AuthenticationSecretsConfig(
	val secrets: AuthenticationSecrets = AuthenticationSecrets()
)

@Serializable
data class AuthenticationSecrets(
	val mongoUser: String = "",
	val mongoPassword: String = "",
	val mongoConnectionStringPrefix: String = "",
	val mongoConnectionString: String = "",
	val jwtSecret: String = "",
	val jwtIssuer: String = "",
	val jwtAudience: String = "",
	val s3AccessKeyId: String = "",
	val s3SecretAccessKey: String = "",
	val createApiKeyUser: String = "",
	val createApiKeyPassword: String = ""
)

// Get from `authenticationSecrets.conf` file
object DevelopmentEnvironmentProvider : EnvironmentProvider {

	init {
		loadConfig()
	}

	// Access the config file from the resources folder
	@OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
	fun loadConfig() {
		val configFile = File("src/main/resources/authenticationSecrets.conf")
		if (!configFile.exists()) {
			println("Config file not found: ${configFile.absolutePath}, attempting to use Environment Variables instead.")

			// Check if environment variables are set
			if (
				System.getenv(MONGO_USER).isNullOrBlank() ||
				System.getenv(MONGO_PASSWORD).isNullOrBlank() ||
				System.getenv(MONGO_CONNECTION_STRING_PREFIX).isNullOrBlank() ||
				System.getenv(MONGO_CONNECTION_STRING).isNullOrBlank() ||
				System.getenv(JWT_SECRET).isNullOrBlank() ||
				System.getenv(JWT_ISSUER).isNullOrBlank() ||
				System.getenv(JWT_AUDIENCE).isNullOrBlank() ||
				System.getenv(S3_ACCESS_KEY_ID).isNullOrBlank() ||
				System.getenv(S3_SECRET_ACCESS_KEY).isNullOrBlank() ||
				System.getenv(CREATE_API_KEY_USER).isNullOrBlank() ||
				System.getenv(CREATE_API_KEY_PASSWORD).isNullOrBlank()
			) {
				throw IllegalStateException(
					"Config file not found and environment variables are not set. " +
							"Please set the environment variables or create a authenticationSecrets.conf file."
				)
			}

			return
		}

		val config = ConfigFactory.parseFile(configFile)
		val authenticationSecretsConfig = Hocon.decodeFromConfig<AuthenticationSecretsConfig>(config)

		// Check if all required fields are set
		if (
			authenticationSecretsConfig.secrets.mongoUser.isBlank() ||
			authenticationSecretsConfig.secrets.mongoPassword.isBlank() ||
			authenticationSecretsConfig.secrets.mongoConnectionString.isBlank() ||
			authenticationSecretsConfig.secrets.mongoConnectionStringPrefix.isBlank() ||
			authenticationSecretsConfig.secrets.jwtSecret.isBlank() ||
			authenticationSecretsConfig.secrets.jwtIssuer.isBlank() ||
			authenticationSecretsConfig.secrets.jwtAudience.isBlank() ||
			authenticationSecretsConfig.secrets.s3AccessKeyId.isBlank() ||
			authenticationSecretsConfig.secrets.s3SecretAccessKey.isBlank() ||
			authenticationSecretsConfig.secrets.createApiKeyUser.isBlank() ||
			authenticationSecretsConfig.secrets.createApiKeyPassword.isBlank()
		) {
			throw IllegalStateException(
				"Config file is missing required fields. " +
						"Please check the authenticationSecrets.conf file."
			)
		}

		// Set the system properties from the config file values
		System.setProperty(MONGO_USER, authenticationSecretsConfig.secrets.mongoUser)
		System.setProperty(MONGO_PASSWORD, authenticationSecretsConfig.secrets.mongoPassword)
		System.setProperty(MONGO_CONNECTION_STRING_PREFIX, authenticationSecretsConfig.secrets.mongoConnectionStringPrefix)
		System.setProperty(MONGO_CONNECTION_STRING, authenticationSecretsConfig.secrets.mongoConnectionString)
		System.setProperty(JWT_SECRET, authenticationSecretsConfig.secrets.jwtSecret)
		System.setProperty(JWT_ISSUER, authenticationSecretsConfig.secrets.jwtIssuer)
		System.setProperty(JWT_AUDIENCE, authenticationSecretsConfig.secrets.jwtAudience)
		System.setProperty(S3_ACCESS_KEY_ID, authenticationSecretsConfig.secrets.s3AccessKeyId)
		System.setProperty(S3_SECRET_ACCESS_KEY, authenticationSecretsConfig.secrets.s3SecretAccessKey)
		System.setProperty(CREATE_API_KEY_USER, authenticationSecretsConfig.secrets.createApiKeyUser)
		System.setProperty(CREATE_API_KEY_PASSWORD, authenticationSecretsConfig.secrets.createApiKeyPassword)
	}

	fun getSystemEnvOrProperty(key: String): String {
		return System.getenv(key) ?: System.getProperty(key)
	}

	override val mongoUser: String
		get() = getSystemEnvOrProperty(MONGO_USER)
	override val mongoPw: String
		get() = getSystemEnvOrProperty(MONGO_PASSWORD)
	override val mongoConnectionStringPrefix: String
		get() = getSystemEnvOrProperty(MONGO_CONNECTION_STRING_PREFIX)
	override val mongoConnectionString: String
		get() = getSystemEnvOrProperty(MONGO_CONNECTION_STRING)
	override val jwtSecret: String
		get() = getSystemEnvOrProperty(JWT_SECRET)
	override val jwtIssuer: String
		get() = getSystemEnvOrProperty(JWT_ISSUER)
	override val jwtAudience: String
		get() = getSystemEnvOrProperty(JWT_AUDIENCE)
	override val s3AccessKeyId: String
		get() = getSystemEnvOrProperty(S3_ACCESS_KEY_ID)
	override val s3SecretAccessKey: String
		get() = getSystemEnvOrProperty(S3_SECRET_ACCESS_KEY)
	override val createApiKeyUser: String
		get() = getSystemEnvOrProperty(CREATE_API_KEY_USER)
	override val createApiKeyPassword	: String
		get() = getSystemEnvOrProperty(CREATE_API_KEY_USER)
}

