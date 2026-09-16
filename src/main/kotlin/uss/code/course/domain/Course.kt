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
    snapshot: CourseSnapshot,
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
    var academicYear: Int = snapshot.academicYear
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "term")
    var term: CourseTerm = snapshot.term
        protected set

    @Column(nullable = false, name = "title_kr")
    var titleKr: String = snapshot.titleKr
        protected set

    @Column(nullable = false, name = "title_en")
    var titleEn: String = snapshot.titleEn
        protected set

    @Column(nullable = false, name = "course_code")
    var courseCode: String = snapshot.courseCode
        protected set

    @Column(nullable = false, name = "haksu_code")
    var haksuCode: String = snapshot.haksuCode
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "college")
    var college: CourseCollege = snapshot.college
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "department")
    var department: CourseDepartment = snapshot.department
        protected set

    @Column(nullable = false, name = "classification_code")
    var classificationCode: String = snapshot.classificationCode
        protected set

    @Column(nullable = false, name = "classification_name")
    var classificationName: String = snapshot.classificationName
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "area")
    var area: CourseArea = snapshot.area
        protected set

    @Column(nullable = false, name = "area_code")
    var areaCode: String = snapshot.areaCode
        protected set

    @Column(nullable = false, name = "area_name")
    var areaName: String = snapshot.areaName
        protected set

    @Column(nullable = false, name = "type_code")
    var typeCode: String = snapshot.typeCode
        protected set

    @Column(nullable = false, name = "type_name")
    var typeName: String = snapshot.typeName
        protected set

    @Column(nullable = false, name = "grade_code")
    var gradeCode: String = snapshot.gradeCode
        protected set

    @Column(nullable = false, name = "grade_name")
    var gradeName: String = snapshot.gradeName
        protected set

    @Column(nullable = false, name = "concentration_code")
    var concentrationCode: String = snapshot.concentrationCode
        protected set

    @Column(nullable = false, name = "concentration_name")
    var concentrationName: String = snapshot.concentrationName
        protected set

    @Column(nullable = false)
    var credits: Int = snapshot.credits
        protected set

    @Column(nullable = false, name = "is_english_course")
    var isEnglishCourse: Boolean = snapshot.isEnglishCourse
        protected set

    @Column(nullable = false, name = "english_code")
    var englishCode: String = snapshot.englishCode
        protected set

    @Column(nullable = false, name = "english_name")
    var englishName: String = snapshot.englishName
        protected set

    @Column(nullable = false, name = "is_huss_course")
    var isHussCourse: Boolean = snapshot.isHussCourse
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

    fun applyUpdate(snapshot: CourseSnapshot): MutableList<CourseFieldChange> {
        val changes = mutableListOf<CourseFieldChange>()

        replaceIfChanged(changes, FIELD_TITLE_KR, titleKr, snapshot.titleKr) { titleKr = it }
        replaceIfChanged(changes, FIELD_TITLE_EN, titleEn, snapshot.titleEn) { titleEn = it }
        replaceIfChanged(changes, FIELD_COURSE_CODE, courseCode, snapshot.courseCode) { courseCode = it }
        replaceIfChanged(changes, FIELD_CREDITS, credits, snapshot.credits) { credits = it }
        replaceIfChanged(changes, FIELD_HUSS_COURSE, isHussCourse, snapshot.isHussCourse) { isHussCourse = it }

        replaceIfChanged(changes, FIELD_COLLEGE, college, snapshot.college, CourseCollege::displayName) { college = it }
        replaceIfChanged(changes, FIELD_DEPARTMENT, department, snapshot.department, CourseDepartment::displayName) { department = it }

        replaceClassification(changes, snapshot)
        replaceArea(changes, snapshot)
        replaceType(changes, snapshot)
        replaceGrade(changes, snapshot)
        replaceConcentration(changes, snapshot)
        replaceEnglish(changes, snapshot)

        return changes
    }

    fun replaceSchedules(schedules: List<CourseSchedule>) {
        this.schedules.clear()
        schedules.forEach(this::addCourseSchedule)
    }

    fun close() {
        this.status = CLOSED
    }

    fun reopen() {
        this.status = ACTIVE
    }

    fun isActive(): Boolean = status == ACTIVE

    fun addCourseSchedule(courseSchedule: CourseSchedule) {
        this.schedules.add(courseSchedule)
        courseSchedule.addCourse(this)
    }

    fun is75MinLesson(): Boolean = schedules.any(CourseSchedule::is75MinLesson)

    fun isRegisterable(): Boolean = currentEnrollment < maxCapacity

    private fun replaceClassification(
        changes: MutableList<CourseFieldChange>,
        snapshot: CourseSnapshot,
    ) {
        if (isSameCodePair(classificationCode, classificationName, snapshot.classificationCode, snapshot.classificationName)) {
            return
        }

        changes.add(CourseFieldChange.of(FIELD_CLASSIFICATION, classificationName, snapshot.classificationName))
        this.classificationCode = snapshot.classificationCode
        this.classificationName = snapshot.classificationName
    }

    private fun replaceArea(
        changes: MutableList<CourseFieldChange>,
        snapshot: CourseSnapshot,
    ) {
        if (isSameCodePair(areaCode, areaName, snapshot.areaCode, snapshot.areaName)) {
            return
        }

        changes.add(CourseFieldChange.of(FIELD_AREA, areaName, snapshot.areaName))
        this.area = snapshot.area
        this.areaCode = snapshot.areaCode
        this.areaName = snapshot.areaName
    }

    private fun replaceType(
        changes: MutableList<CourseFieldChange>,
        snapshot: CourseSnapshot,
    ) {
        if (isSameCodePair(typeCode, typeName, snapshot.typeCode, snapshot.typeName)) {
            return
        }

        changes.add(CourseFieldChange.of(FIELD_TYPE, typeName, snapshot.typeName))
        this.typeCode = snapshot.typeCode
        this.typeName = snapshot.typeName
    }

    private fun replaceGrade(
        changes: MutableList<CourseFieldChange>,
        snapshot: CourseSnapshot,
    ) {
        if (isSameCodePair(gradeCode, gradeName, snapshot.gradeCode, snapshot.gradeName)) {
            return
        }

        changes.add(CourseFieldChange.of(FIELD_GRADE, gradeName, snapshot.gradeName))
        this.gradeCode = snapshot.gradeCode
        this.gradeName = snapshot.gradeName
    }

    private fun replaceConcentration(
        changes: MutableList<CourseFieldChange>,
        snapshot: CourseSnapshot,
    ) {
        if (isSameCodePair(concentrationCode, concentrationName, snapshot.concentrationCode, snapshot.concentrationName)) {
            return
        }

        changes.add(CourseFieldChange.of(FIELD_CONCENTRATION, concentrationName, snapshot.concentrationName))
        this.concentrationCode = snapshot.concentrationCode
        this.concentrationName = snapshot.concentrationName
    }

    private fun replaceEnglish(
        changes: MutableList<CourseFieldChange>,
        snapshot: CourseSnapshot,
    ) {
        if (isEnglishCourse == snapshot.isEnglishCourse &&
            isSameCodePair(englishCode, englishName, snapshot.englishCode, snapshot.englishName)
        ) {
            return
        }

        changes.add(CourseFieldChange.of(FIELD_ENGLISH_COURSE, englishName, snapshot.englishName))
        this.isEnglishCourse = snapshot.isEnglishCourse
        this.englishCode = snapshot.englishCode
        this.englishName = snapshot.englishName
    }

    private fun isSameCodePair(
        currentCode: String,
        currentName: String,
        updatedCode: String,
        updatedName: String,
    ): Boolean = currentCode == updatedCode && currentName == updatedName

    private fun <T> replaceIfChanged(
        changes: MutableList<CourseFieldChange>,
        field: String,
        current: T,
        updated: T,
        toText: (T) -> String = { it.toString() },
        setter: (T) -> Unit,
    ) {
        if (current == updated) {
            return
        }

        changes.add(CourseFieldChange.of(field, toText(current), toText(updated)))
        setter(updated)
    }

    companion object {
        private const val INITIAL_ENROLLMENT = 0
        private const val INITIAL_CART_COUNT = 0

        private const val FIELD_TITLE_KR = "titleKr"
        private const val FIELD_TITLE_EN = "titleEn"
        private const val FIELD_COURSE_CODE = "courseCode"
        private const val FIELD_CREDITS = "credits"
        private const val FIELD_COLLEGE = "college"
        private const val FIELD_DEPARTMENT = "department"
        private const val FIELD_CLASSIFICATION = "classification"
        private const val FIELD_AREA = "area"
        private const val FIELD_TYPE = "type"
        private const val FIELD_GRADE = "grade"
        private const val FIELD_CONCENTRATION = "concentration"
        private const val FIELD_ENGLISH_COURSE = "isEnglishCourse"
        private const val FIELD_HUSS_COURSE = "isHussCourse"

        @JvmStatic
        fun create(
            snapshot: CourseSnapshot,
            maxCapacity: Int,
        ): Course = Course(
            snapshot = snapshot,
            maxCapacity = maxCapacity,
        )
    }
}
