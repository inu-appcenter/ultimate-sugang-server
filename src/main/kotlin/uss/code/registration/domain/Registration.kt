package uss.code.registration.domain

import jakarta.persistence.*
import uss.code.course.domain.Course
import uss.code.member.domain.Member
import java.time.LocalDateTime

@Entity
@Table(
    name = "registrations",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["member_id", "course_id"]),
    ],
)
class Registration private constructor(
    member: Member,
    course: Course,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "member_id")
    var member: Member = member
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "course_id")
    var course: Course = course
        protected set

    @Column(nullable = false, name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now()
        protected set

    companion object {
        @JvmStatic
        fun create(
            member: Member,
            course: Course,
        ): Registration {
            return Registration(
                member = member,
                course = course,
            )
        }
    }
}
