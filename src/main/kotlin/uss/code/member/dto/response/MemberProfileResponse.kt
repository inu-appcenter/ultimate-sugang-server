package uss.code.member.dto.response

import uss.code.member.domain.Member

data class MemberProfileResponse(
    val email: String,

    val department: String,

    val studentId: String,

    val name: String,

    val grade: String,

    val academicStatus: String,

    val gpa: Double,

    val creditLimit: Int,
) {
    companion object {
        fun of(member: Member): MemberProfileResponse {
            return MemberProfileResponse(
                email = member.email,
                department = member.department.displayName,
                studentId = member.studentId,
                name = member.name,
                grade = member.grade.displayName,
                academicStatus = member.academicStatus.displayName,
                gpa = member.lastSemesterGpa,
                creditLimit = member.maxCredit,
            )
        }
    }
}
