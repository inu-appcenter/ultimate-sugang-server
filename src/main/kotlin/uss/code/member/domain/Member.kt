package uss.code.member.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "members",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["email"]),
        UniqueConstraint(columnNames = ["student_id"]),
    ],
)
class Member private constructor(
    email: String,
    password: String,
    studentId: String,
    name: String,
    college: MemberCollege,
    department: MemberDepartment,
    grade: MemberGrade,
    academicStatus: AcademicStatus,
    lastSemesterGpa: Double,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L
        protected set

    @Column(nullable = false, name = "email")
    var email: String = email
        protected set

    @Column(nullable = false, name = "password")
    var password: String = password
        protected set

    @Column(nullable = false, name = "student_id")
    var studentId: String = studentId
        protected set

    @Column(nullable = false)
    var name: String = name
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "college")
    var college: MemberCollege = college
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "department")
    var department: MemberDepartment = department
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "grade")
    var grade: MemberGrade = grade
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "academic_status")
    var academicStatus: AcademicStatus = academicStatus
        protected set

    @Column(nullable = false, name = "last_semester_gpa")
    var lastSemesterGpa: Double = lastSemesterGpa
        protected set

    @Column(nullable = false, name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now()
        protected set

    @Column(nullable = false, name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
        protected set

    val maxCredit: Int
        get() {
            return when {
                lastSemesterGpa >= 4.0 -> 24
                lastSemesterGpa >= 3.5 -> 21
                else -> 19
            }
        }

    fun updateDepartment(department: MemberDepartment) {
        this.department = department
        this.college = department.memberCollege
        this.updatedAt = LocalDateTime.now()
    }

    companion object {
        @JvmStatic
        fun create(
            email: String,
            encodedPassword: String,
            studentId: String,
            name: String,
            department: MemberDepartment,
            grade: MemberGrade,
            academicStatus: AcademicStatus,
            lastSemesterGpa: Double,
        ): Member {
            return Member(
                email = email,
                password = encodedPassword,
                studentId = studentId,
                name = name,
                college = department.memberCollege,
                department = department,
                grade = grade,
                academicStatus = academicStatus,
                lastSemesterGpa = lastSemesterGpa,
            )
        }
    }
}
