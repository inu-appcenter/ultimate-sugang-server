package uss.code.global.infra

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import uss.code.global.annotation.EnumValidation

class EnumValidator : ConstraintValidator<EnumValidation, String> {
    private lateinit var validValues: Set<String>

    override fun initialize(constraintAnnotation: EnumValidation) {
        validValues = constraintAnnotation.target.java.enumConstants
            .map { it.name.uppercase() }
            .toSet()
    }

    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext,
    ): Boolean {
        if (value == null) {
            return true
        }

        return value.uppercase() in validValues
    }
}
