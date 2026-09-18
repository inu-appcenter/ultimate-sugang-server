package uss.code.global.infra

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import uss.code.global.annotation.ParamValidation

class ParamValidator : ConstraintValidator<ParamValidation, String> {
    private var minLength: Int = 0
    private var maxLength: Int = 0

    override fun initialize(constraintAnnotation: ParamValidation) {
        minLength = constraintAnnotation.minLength
        maxLength = constraintAnnotation.maxLength
    }

    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext,
    ): Boolean {
        if (value == null) {
            return true
        }

        if (value.all(Character::isWhitespace)) {
            return false
        }

        val trimmed = value.trim { it <= ' ' }
        return trimmed.length in minLength..maxLength
    }
}
