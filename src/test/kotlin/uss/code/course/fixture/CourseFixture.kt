package uss.code.course.fixture

import org.springframework.test.util.ReflectionTestUtils
import uss.code.course.domain.Course
import uss.code.course.domain.CourseArea
import uss.code.course.domain.CourseClassification
import uss.code.course.domain.CourseCollege
import uss.code.course.domain.CourseDepartment
import uss.code.course.domain.CourseGrade
import uss.code.course.domain.CourseTerm
import uss.code.course.domain.CourseType

object CourseFixture {
    private const val DEFAULT_ACADEMIC_YEAR = 2026
    private val DEFAULT_TERM = CourseTerm.SECOND
    private const val DEFAULT_TITLE_KR = "데이터구조"
    private const val DEFAULT_TITLE_EN = "Data Structure"
    private const val DEFAULT_COURSE_CODE = "CSE2010"
    private const val DEFAULT_HAKSU_CODE = "CSE2010001"
    private val DEFAULT_COLLEGE = CourseCollege.INFORMATION_TECHNOLOGY
    private val DEFAULT_DEPARTMENT = CourseDepartment.COMPUTER_ENGINEERING
    private val DEFAULT_CLASSIFICATION = CourseClassification.MAJOR_CORE
    private val DEFAULT_AREA = CourseArea.MAJOR_CORE
    private val DEFAULT_TYPE = CourseType.LECTURE
    private val DEFAULT_GRADE = CourseGrade.SOPHOMORE
    private const val DEFAULT_CREDITS = 3
    private const val DEFAULT_IS_ENGLISH = false
    private const val NOT_ENGLISH_CODE = "0"
    private const val NOT_ENGLISH_NAME = "비대상"
    private const val ENGLISH_CODE = "1"
    private const val ENGLISH_NAME = "원어강의(EN)"
    private const val DEFAULT_CONCENTRATION_CODE = "0"
    private const val DEFAULT_CONCENTRATION_NAME = "일반(1~15주)"
    private const val DEFAULT_IS_HUSS = false
    private const val DEFAULT_MAX_CAPACITY = 50
    private const val DEFAULT_CURRENT_ENROLLMENT = 30

    fun createCourse(): Course {
        return createCourse(
            DEFAULT_TITLE_KR,
            DEFAULT_TITLE_EN,
            DEFAULT_COURSE_CODE,
            DEFAULT_HAKSU_CODE,
            DEFAULT_COLLEGE,
            DEFAULT_DEPARTMENT,
            DEFAULT_CLASSIFICATION,
            DEFAULT_AREA,
            DEFAULT_TYPE,
            DEFAULT_GRADE,
            DEFAULT_CREDITS,
            DEFAULT_IS_ENGLISH,
            DEFAULT_MAX_CAPACITY,
            DEFAULT_CURRENT_ENROLLMENT,
        )
    }

    fun createCourseWithDetails(
        titleKr: String,
        titleEn: String,
        courseCode: String,
        haksuCode: String,
        grade: CourseGrade,
    ): Course {
        return createCourse(
            titleKr,
            titleEn,
            courseCode,
            haksuCode,
            DEFAULT_COLLEGE,
            DEFAULT_DEPARTMENT,
            DEFAULT_CLASSIFICATION,
            DEFAULT_AREA,
            DEFAULT_TYPE,
            grade,
            DEFAULT_CREDITS,
            DEFAULT_IS_ENGLISH,
            DEFAULT_MAX_CAPACITY,
            DEFAULT_CURRENT_ENROLLMENT,
        )
    }

    fun createCourseWithDepartmentAndDetails(
        titleKr: String,
        titleEn: String,
        courseCode: String,
        haksuCode: String,
        department: CourseDepartment,
        grade: CourseGrade,
    ): Course {
        return createCourse(
            titleKr,
            titleEn,
            courseCode,
            haksuCode,
            department.courseCollege,
            department,
            DEFAULT_CLASSIFICATION,
            DEFAULT_AREA,
            DEFAULT_TYPE,
            grade,
            DEFAULT_CREDITS,
            DEFAULT_IS_ENGLISH,
            DEFAULT_MAX_CAPACITY,
            DEFAULT_CURRENT_ENROLLMENT,
        )
    }

    fun createHussCourse(
        titleKr: String,
        titleEn: String,
        courseCode: String,
        haksuCode: String,
        department: CourseDepartment,
    ): Course {
        return create(
            titleKr,
            titleEn,
            courseCode,
            haksuCode,
            department.courseCollege,
            department,
            DEFAULT_CLASSIFICATION,
            DEFAULT_AREA,
            DEFAULT_TYPE,
            DEFAULT_GRADE,
            DEFAULT_CREDITS,
            DEFAULT_IS_ENGLISH,
            true,
            DEFAULT_MAX_CAPACITY,
            DEFAULT_CURRENT_ENROLLMENT,
        )
    }

    fun createCourse(
        titleKr: String,
        titleEn: String,
        courseCode: String,
        haksuCode: String,
        college: CourseCollege,
        department: CourseDepartment,
        classification: CourseClassification,
        area: CourseArea,
        type: CourseType,
        grade: CourseGrade,
        credits: Int,
        isEnglishCourse: Boolean,
        maxCapacity: Int,
        currentEnrollment: Int,
    ): Course {
        return create(
            titleKr,
            titleEn,
            courseCode,
            haksuCode,
            college,
            department,
            classification,
            area,
            type,
            grade,
            credits,
            isEnglishCourse,
            DEFAULT_IS_HUSS,
            maxCapacity,
            currentEnrollment,
        )
    }

    private fun create(
        titleKr: String,
        titleEn: String,
        courseCode: String,
        haksuCode: String,
        college: CourseCollege,
        department: CourseDepartment,
        classification: CourseClassification,
        area: CourseArea,
        type: CourseType,
        grade: CourseGrade,
        credits: Int,
        isEnglishCourse: Boolean,
        isHussCourse: Boolean,
        maxCapacity: Int,
        currentEnrollment: Int,
    ): Course {
        val course = Course.create(
            academicYear = DEFAULT_ACADEMIC_YEAR,
            term = DEFAULT_TERM,
            titleKr = titleKr,
            titleEn = titleEn,
            courseCode = courseCode,
            haksuCode = haksuCode,
            college = college,
            department = department,
            classificationCode = classification.code,
            classificationName = classification.displayName,
            area = area,
            areaCode = area.code,
            areaName = area.displayName,
            typeCode = type.code,
            typeName = type.displayName,
            gradeCode = grade.code,
            gradeName = grade.displayName,
            concentrationCode = DEFAULT_CONCENTRATION_CODE,
            concentrationName = DEFAULT_CONCENTRATION_NAME,
            credits = credits,
            isEnglishCourse = isEnglishCourse,
            englishCode = if (isEnglishCourse) ENGLISH_CODE else NOT_ENGLISH_CODE,
            englishName = if (isEnglishCourse) ENGLISH_NAME else NOT_ENGLISH_NAME,
            isHussCourse = isHussCourse,
            maxCapacity = maxCapacity,
        )
        ReflectionTestUtils.setField(course, "currentEnrollment", currentEnrollment)

        return course
    }
}
