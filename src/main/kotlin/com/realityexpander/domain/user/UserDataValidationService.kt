package com.realityexpander.domain.user

import org.apache.commons.validator.routines.DomainValidator
import org.apache.commons.validator.routines.EmailValidator

class UserDataValidationService {

    fun validateUserData(fullName: String, email: String, password: String): ValidationResult {
        val fullNameError = if(isValidFullName(fullName)) {
            null
        } else {
            "The name must be between $MIN_NAME_LENGTH and $MAX_NAME_LENGTH characters long."
        }
        val emailError = if(isValidEmail(email.trim().lowercase())) {
            null
        } else {
            "That is not a valid email address"
        }
        val passwordError = if(isValidPassword(password)) {
            null
        } else {
            "The password needs to be at least $MIN_PASSWORD_LENGTH " +
                    "characters long and contain uppercase, lowercase and a number."
        }

        return ValidationResult(fullNameError, emailError, passwordError)
    }

    private fun isValidEmail(email: String): Boolean {
        return EmailValidator(false, false, DomainValidator.getInstance()).isValid(email)
    }

    private fun isValidFullName(fullName: String): Boolean {
        return fullName.trim().length in (MIN_NAME_LENGTH..MAX_NAME_LENGTH)
    }

    private fun isValidPassword(password: String): Boolean {
        val containsLowerCase = password.any { it.isLowerCase() }
        val containsUpperCase = password.any { it.isUpperCase() }
        val containsNumber = password.any { it.isDigit() }
        val isValidLength = password.length in (MIN_PASSWORD_LENGTH..MAX_PASSWORD_LENGTH)

        return containsLowerCase && containsUpperCase && containsNumber && isValidLength
    }

    data class ValidationResult(
        val fullNameError: String?,
        val emailError: String?,
        val passwordError: String?
    )

    companion object {
        const val MIN_NAME_LENGTH = 4
        const val MAX_NAME_LENGTH = 50

        const val MIN_PASSWORD_LENGTH = 9
        const val MAX_PASSWORD_LENGTH = 50
    }
}