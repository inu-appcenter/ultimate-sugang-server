package uss.code.member.fixture

import uss.code.member.domain.AcademicStatus
import uss.code.member.domain.Member
import uss.code.member.domain.MemberDepartment
import uss.code.member.domain.MemberGrade
import java.util.concurrent.atomic.AtomicLong

object MemberFixture {
    private const val ENCODED_PASSWORD = "\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"

    private val EMAIL_SEQUENCE = AtomicLong()
    private val STUDENT_ID_SEQUENCE = AtomicLong()

    fun createMember(): Member {
        return createMember(
            nextStudentId(),
            "홍길동",
            MemberDepartment.COMPUTER_ENGINEERING,
            MemberGrade.JUNIOR,
            AcademicStatus.ENROLLED,
            3.5,
        )
    }

    fun createMember(
        studentId: String,
        name: String,
        department: MemberDepartment,
        grade: MemberGrade,
        academicStatus: AcademicStatus,
        lastSemesterGpa: Double,
    ): Member {
        return createMember(
            "member${EMAIL_SEQUENCE.incrementAndGet()}@inu.ac.kr",
            ENCODED_PASSWORD,
            studentId,
            name,
            department,
            grade,
            academicStatus,
            lastSemesterGpa,
        )
    }

    fun nextStudentId(): String {
        return "2024${"%05d".format(STUDENT_ID_SEQUENCE.incrementAndGet())}"
    }

    fun createMember(
        email: String,
        password: String,
        studentId: String,
        name: String,
        department: MemberDepartment,
        grade: MemberGrade,
        academicStatus: AcademicStatus,
        lastSemesterGpa: Double,
    ): Member {
        return Member.create(
            email = email,
            encodedPassword = password,
            studentId = studentId,
            name = name,
            department = department,
            grade = grade,
            academicStatus = academicStatus,
            lastSemesterGpa = lastSemesterGpa,
        )
    }
}
