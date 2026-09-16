package uss.code.course.dto.internal

@JvmRecord
data class CourseCategoryDto(
    val classificationCode: String,
    val classificationName: String,
    val areaCode: String,
    val areaName: String,
)
