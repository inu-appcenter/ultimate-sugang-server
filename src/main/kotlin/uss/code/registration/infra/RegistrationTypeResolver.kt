package uss.code.registration.infra

import uss.code.course.domain.Course
import uss.code.course.domain.CourseClassification.GENERAL_ELECTIVE
import uss.code.course.domain.CourseClassification.MAJOR_ADVANCED
import uss.code.course.domain.CourseClassification.MAJOR_BASIC
import uss.code.course.domain.CourseClassification.MAJOR_CORE
import uss.code.course.domain.CourseDepartment
import uss.code.member.domain.Member

object RegistrationTypeResolver {
    private val GENERAL_ELECTIVE_NAME = GENERAL_ELECTIVE.displayName
    private val MAJOR_CLASSIFICATION_CODES = setOf(MAJOR_BASIC.code, MAJOR_CORE.code, MAJOR_ADVANCED.code)

    fun resolve(
        member: Member,
        course: Course,
    ): String {
        if (isOtherDepartmentMajor(member, course)) {
            return GENERAL_ELECTIVE_NAME
        }

        return course.classificationName
    }

    private fun isOtherDepartmentMajor(
        member: Member,
        course: Course,
    ): Boolean {
        val ownDepartment = course.department in CourseDepartment.ownedBy(member.department)

        return !ownDepartment && course.classificationCode in MAJOR_CLASSIFICATION_CODES
    }
}
