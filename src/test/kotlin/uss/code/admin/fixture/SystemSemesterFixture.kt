package uss.code.admin.fixture

import uss.code.admin.domain.SystemSemester
import uss.code.course.domain.CourseTerm

object SystemSemesterFixture {
    private const val DEFAULT_ACADEMIC_YEAR = 2026
    private val DEFAULT_TERM = CourseTerm.SECOND

    fun createSystemSemester(): SystemSemester {
        return createSystemSemester(DEFAULT_ACADEMIC_YEAR, DEFAULT_TERM)
    }

    fun createSystemSemester(
        academicYear: Int,
        term: CourseTerm,
    ): SystemSemester {
        return SystemSemester.create(
            academicYear = academicYear,
            term = term,
        )
    }
}
