package uss.code.global.annotation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import uss.code.global.infra.EnumValidator
import kotlin.reflect.KClass

@Constraint(validatedBy = [EnumValidator::class])
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class EnumValidation(
    val message: String = "유효하지 않은 열거 타입의 값입니다.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
    val target: KClass<out Enum<*>>,
)
