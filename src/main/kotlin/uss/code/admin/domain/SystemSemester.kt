package uss.code.admin.domain

import jakarta.persistence.*
import uss.code.course.domain.CourseTerm
import java.time.LocalDateTime

@Entity
@Table(name = "system_semesters")
class SystemSemester private constructor(
    academicYear: Int,
    term: CourseTerm,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L
        protected set

    @Column(nullable = false, name = "academic_year")
    var academicYear: Int = academicYear
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "term")
    var term: CourseTerm = term
        protected set

    @Column(nullable = false, name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
        protected set

    fun change(
        academicYear: Int,
        term: CourseTerm,
    ) {
        this.academicYear = academicYear
        this.term = term
        this.updatedAt = LocalDateTime.now()
    }

    companion object {
        @JvmStatic
        fun create(
            academicYear: Int,
            term: CourseTerm,
        ): SystemSemester {
            return SystemSemester(
                academicYear = academicYear,
                term = term,
            )
        }
    }
}
