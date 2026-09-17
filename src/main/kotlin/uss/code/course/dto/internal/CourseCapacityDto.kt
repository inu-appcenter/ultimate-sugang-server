package uss.code.course.dto.internal

@JvmRecord
data class CourseCapacityDto(
    val id: Long,
    val currentEnrollment: Int,
    val maxCapacity: Int,
    val cartCount: Int,
) {
    fun isRegisterable(): Boolean {
        return currentEnrollment < maxCapacity
    }
}
