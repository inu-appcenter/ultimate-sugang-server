package uss.code.global.annotation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import uss.code.global.infra.ParamValidator
import kotlin.reflect.KClass

@Constraint(validatedBy = [ParamValidator::class])
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ParamValidation(
    val message: String = "유효하지 않은 파라미터입니다.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
    val minLength: Int = 1,
    val maxLength: Int = Int.MAX_VALUE,
)
