package uss.code.course.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uss.code.course.domain.Course
import uss.code.course.domain.CourseDepartment
import uss.code.course.domain.CourseTerm
import uss.code.course.dto.internal.CourseCapacityDto
import uss.code.course.dto.internal.CourseCategoryDto
import uss.code.course.dto.internal.CourseTermInfoDto
import java.util.Optional

interface CourseRepository : JpaRepository<Course, Long> {
    @Query("""
        SELECT c
        FROM Course c
        WHERE c.department = :department
          AND c.status = uss.code.course.domain.CourseStatus.ACTIVE
        ORDER BY c.gradeCode, c.classificationCode, c.haksuCode
    """)
    fun findByDepartment(@Param("department") department: CourseDepartment): List<Course>

    @Query("""
        SELECT c
        FROM Course c
        WHERE c.department IN :departments
          AND c.status = uss.code.course.domain.CourseStatus.ACTIVE
        ORDER BY c.gradeCode, c.classificationCode, c.haksuCode
    """)
    fun findByDepartmentIn(@Param("departments") departments: List<CourseDepartment>): List<Course>

    @Query("""
        SELECT new uss.code.course.dto.internal.CourseCapacityDto(c.id, c.currentEnrollment, c.maxCapacity, c.cartCount)
        FROM Course c
        WHERE c.department IN :departments
          AND c.status = uss.code.course.domain.CourseStatus.ACTIVE
    """)
    fun findCapacitiesByDepartmentIn(@Param("departments") departments: List<CourseDepartment>): List<CourseCapacityDto>

    @Query("""
        SELECT c
        FROM Course c
        WHERE c.classificationCode = :classificationCode
          AND c.status = uss.code.course.domain.CourseStatus.ACTIVE
        ORDER BY c.gradeCode, c.classificationCode, c.haksuCode
    """)
    fun findByClassificationCode(@Param("classificationCode") classificationCode: String): List<Course>

    @Query("""
        SELECT new uss.code.course.dto.internal.CourseCapacityDto(c.id, c.currentEnrollment, c.maxCapacity, c.cartCount)
        FROM Course c
        WHERE c.classificationCode = :classificationCode
          AND c.status = uss.code.course.domain.CourseStatus.ACTIVE
    """)
    fun findCapacitiesByClassificationCode(@Param("classificationCode") classificationCode: String): List<CourseCapacityDto>

    @Query(value = """
        SELECT DISTINCT c.*
        FROM courses c
        WHERE MATCH(c.course_code, c.haksu_code, c.title_kr, c.title_en) AGAINST(:keyword IN BOOLEAN MODE)
          AND c.status = 'ACTIVE'
        ORDER BY c.grade_code,
                 MATCH(c.course_code, c.haksu_code, c.title_kr, c.title_en) AGAINST(:keyword IN BOOLEAN MODE) DESC,
                 c.haksu_code
    """, nativeQuery = true)
    fun findByKeyword(@Param("keyword") keyword: String): List<Course>

    @Query("""
        SELECT c
        FROM Course c
        LEFT JOIN FETCH c.schedules
        WHERE c.id = :id
    """)
    fun findByIdWithSchedules(@Param("id") id: Long): Optional<Course>

    @Query("""
        SELECT DISTINCT new uss.code.course.dto.internal.CourseCategoryDto(
            c.classificationCode, c.classificationName, c.areaCode, c.areaName)
        FROM Course c
        ORDER BY c.classificationCode, c.areaCode
    """)
    fun findCategories(): List<CourseCategoryDto>

    @Query("""
        SELECT DISTINCT new uss.code.course.dto.internal.CourseTermInfoDto(c.academicYear, c.term)
        FROM Course c
        ORDER BY c.academicYear DESC, c.term
    """)
    fun findTerms(): List<CourseTermInfoDto>

    @Query("""
        SELECT c
        FROM Course c
        WHERE c.isHussCourse = true
          AND c.status = uss.code.course.domain.CourseStatus.ACTIVE
        ORDER BY c.gradeCode, c.classificationCode, c.haksuCode
    """)
    fun findHussCourses(): List<Course>

    @Query("""
        SELECT COUNT(c)
        FROM Course c
        WHERE c.academicYear = :academicYear
          AND c.term = :term
    """)
    fun countBySemester(
        @Param("academicYear") academicYear: Int,
        @Param("term") term: CourseTerm,
    ): Long

    @Query("""
        SELECT c
        FROM Course c
        LEFT JOIN FETCH c.schedules
        WHERE c.academicYear = :academicYear
          AND c.term = :term
    """)
    fun findAllBySemesterWithSchedules(
        @Param("academicYear") academicYear: Int,
        @Param("term") term: CourseTerm,
    ): List<Course>

    @Modifying(flushAutomatically = true)
    @Query("""
        UPDATE Course c
        SET c.currentEnrollment = c.currentEnrollment + 1
        WHERE c.id = :id
          AND c.currentEnrollment < c.maxCapacity
    """)
    fun increaseEnrollmentWithinCapacity(@Param("id") id: Long): Int

    @Modifying(flushAutomatically = true)
    @Query("""
        UPDATE Course c
        SET c.currentEnrollment = c.currentEnrollment - 1
        WHERE c.id = :id
          AND c.currentEnrollment > 0
    """)
    fun decreaseEnrollmentAboveZero(@Param("id") id: Long): Int

    @Modifying(flushAutomatically = true)
    @Query("""
        UPDATE Course c
        SET c.cartCount = c.cartCount + 1
        WHERE c.id = :id
    """)
    fun increaseCartCount(@Param("id") id: Long)

    @Modifying(flushAutomatically = true)
    @Query("""
        UPDATE Course c
        SET c.cartCount = c.cartCount - 1
        WHERE c.id = :id
          AND c.cartCount > 0
    """)
    fun decreaseCartCountAboveZero(@Param("id") id: Long): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        DELETE FROM Course c
        WHERE c.academicYear = :academicYear
          AND c.term = :term
    """)
    fun deleteBySemester(
        @Param("academicYear") academicYear: Int,
        @Param("term") term: CourseTerm,
    )
}
