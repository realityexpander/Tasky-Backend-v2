package com.realityexpander.fakes

import com.realityexpander.domain.util.EnvironmentProvider

data object TestEnvironmentProvider : EnvironmentProvider {
	override val mongoUser: String
		get() = "test-user"
	override val mongoPw: String
		get() = "test-pw"
	override val mongoConnectionStringPrefix: String
		get() = "test-connection-string-prefix"
	override val mongoConnectionString: String
		get() = "test-connection-string"
	override val jwtSecret: String
		get() = "test-secret"
	override val jwtIssuer: String
		get() = "test-issuer"
	override val jwtAudience: String
		get() = "test-audience"
	override val s3AccessKeyId: String
		get() = "test-access-key-id"
	override val s3SecretAccessKey: String
		get() = "test-secret-access-key"
	override val createApiKeyUser: String
		get() = "test-user"
	override val createApiKeyPassword: String
		get() = "test-password"
}
