package uss.code.course.domain

import jakarta.persistence.*
import jakarta.persistence.CascadeType.PERSIST
import jakarta.persistence.CascadeType.REMOVE
import org.hibernate.annotations.BatchSize
import uss.code.course.domain.CourseStatus.ACTIVE
import uss.code.course.domain.CourseStatus.CLOSED

@Entity
@Table(name = "courses")
class Course private constructor(
    academicYear: Int,
    term: CourseTerm,
    titleKr: String,
    titleEn: String,
    courseCode: String,
    haksuCode: String,
    college: CourseCollege,
    department: CourseDepartment,
    classificationCode: String,
    classificationName: String,
    area: CourseArea,
    areaCode: String,
    areaName: String,
    typeCode: String,
    typeName: String,
    gradeCode: String,
    gradeName: String,
    concentrationCode: String,
    concentrationName: String,
    credits: Int,
    isEnglishCourse: Boolean,
    englishCode: String,
    englishName: String,
    isHussCourse: Boolean,
    maxCapacity: Int,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L
        protected set

    @OneToMany(mappedBy = "course", cascade = [PERSIST, REMOVE], orphanRemoval = true)
    @BatchSize(size = 1000)
    var schedules: MutableList<CourseSchedule> = mutableListOf()
        protected set

    @Column(nullable = false, name = "academic_year")
    var academicYear: Int = academicYear
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "term")
    var term: CourseTerm = term
        protected set

    @Column(nullable = false, name = "title_kr")
    var titleKr: String = titleKr
        protected set

    @Column(nullable = false, name = "title_en")
    var titleEn: String = titleEn
        protected set

    @Column(nullable = false, name = "course_code")
    var courseCode: String = courseCode
        protected set

    @Column(nullable = false, name = "haksu_code")
    var haksuCode: String = haksuCode
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "college")
    var college: CourseCollege = college
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "department")
    var department: CourseDepartment = department
        protected set

    @Column(nullable = false, name = "classification_code")
    var classificationCode: String = classificationCode
        protected set

    @Column(nullable = false, name = "classification_name")
    var classificationName: String = classificationName
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "area")
    var area: CourseArea = area
        protected set

    @Column(nullable = false, name = "area_code")
    var areaCode: String = areaCode
        protected set

    @Column(nullable = false, name = "area_name")
    var areaName: String = areaName
        protected set

    @Column(nullable = false, name = "type_code")
    var typeCode: String = typeCode
        protected set

    @Column(nullable = false, name = "type_name")
    var typeName: String = typeName
        protected set

    @Column(nullable = false, name = "grade_code")
    var gradeCode: String = gradeCode
        protected set

    @Column(nullable = false, name = "grade_name")
    var gradeName: String = gradeName
        protected set

    @Column(nullable = false, name = "concentration_code")
    var concentrationCode: String = concentrationCode
        protected set

    @Column(nullable = false, name = "concentration_name")
    var concentrationName: String = concentrationName
        protected set

    @Column(nullable = false)
    var credits: Int = credits
        protected set

    @Column(nullable = false, name = "is_english_course")
    var isEnglishCourse: Boolean = isEnglishCourse
        protected set

    @Column(nullable = false, name = "english_code")
    var englishCode: String = englishCode
        protected set

    @Column(nullable = false, name = "english_name")
    var englishName: String = englishName
        protected set

    @Column(nullable = false, name = "is_huss_course")
    var isHussCourse: Boolean = isHussCourse
        protected set

    @Column(nullable = false, name = "max_capacity")
    var maxCapacity: Int = maxCapacity
        protected set

    @Column(nullable = false, name = "current_enrollment")
    var currentEnrollment: Int = INITIAL_ENROLLMENT
        protected set

    @Column(nullable = false, name = "cart_count")
    var cartCount: Int = INITIAL_CART_COUNT
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status")
    var status: CourseStatus = ACTIVE
        protected set

    fun close() {
        this.status = CLOSED
    }

    fun isActive(): Boolean {
        return status == ACTIVE
    }

    fun addCourseSchedule(courseSchedule: CourseSchedule) {
        this.schedules.add(courseSchedule)
        courseSchedule.addCourse(this)
    }

    fun is75MinLesson(): Boolean {
        return schedules.any(CourseSchedule::is75MinLesson)
    }

    fun isRegisterable(): Boolean {
        return currentEnrollment < maxCapacity
    }

    companion object {
        private const val INITIAL_ENROLLMENT = 0
        private const val INITIAL_CART_COUNT = 0

        fun create(
            academicYear: Int,
            term: CourseTerm,
            titleKr: String,
            titleEn: String,
            courseCode: String,
            haksuCode: String,
            college: CourseCollege,
            department: CourseDepartment,
            classificationCode: String,
            classificationName: String,
            area: CourseArea,
            areaCode: String,
            areaName: String,
            typeCode: String,
            typeName: String,
            gradeCode: String,
            gradeName: String,
            concentrationCode: String,
            concentrationName: String,
            credits: Int,
            isEnglishCourse: Boolean,
            englishCode: String,
            englishName: String,
            isHussCourse: Boolean,
            maxCapacity: Int,
        ): Course {
            return Course(
                academicYear = academicYear,
                term = term,
                titleKr = titleKr,
                titleEn = titleEn,
                courseCode = courseCode,
                haksuCode = haksuCode,
                college = college,
                department = department,
                classificationCode = classificationCode,
                classificationName = classificationName,
                area = area,
                areaCode = areaCode,
                areaName = areaName,
                typeCode = typeCode,
                typeName = typeName,
                gradeCode = gradeCode,
                gradeName = gradeName,
                concentrationCode = concentrationCode,
                concentrationName = concentrationName,
                credits = credits,
                isEnglishCourse = isEnglishCourse,
                englishCode = englishCode,
                englishName = englishName,
                isHussCourse = isHussCourse,
                maxCapacity = maxCapacity,
            )
        }
    }
}
