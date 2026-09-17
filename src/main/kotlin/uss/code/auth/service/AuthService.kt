package uss.code.auth.service

import org.hibernate.exception.ConstraintViolationException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uss.code.auth.dto.request.LoginRequest
import uss.code.auth.dto.request.SignUpRequest
import uss.code.auth.dto.response.AuthTokenResponse
import uss.code.auth.dto.response.EmailAvailabilityResponse
import uss.code.auth.dto.response.StudentIdAvailabilityResponse
import uss.code.auth.infra.JwtProvider
import uss.code.auth.infra.MemberPasswordEncoder
import uss.code.global.exception.domain.ExceptionCode
import uss.code.global.exception.domain.ExceptionCode.COLLEGE_DEPARTMENT_MISMATCH
import uss.code.global.exception.domain.ExceptionCode.EMAIL_ALREADY_EXISTS
import uss.code.global.exception.domain.ExceptionCode.MEMBER_NOT_FOUND
import uss.code.global.exception.domain.ExceptionCode.PASSWORD_NOT_MATCH
import uss.code.global.exception.domain.ExceptionCode.STUDENT_ID_ALREADY_EXISTS
import uss.code.global.exception.domain.RestApiException
import uss.code.member.domain.AcademicStatus
import uss.code.member.domain.Member
import uss.code.member.domain.MemberCollege
import uss.code.member.domain.MemberDepartment
import uss.code.member.domain.MemberGrade
import uss.code.member.repository.MemberRepository

@Service
class AuthService(
    private val memberRepository: MemberRepository,

    private val jwtProvider: JwtProvider,
    private val passwordEncoder: MemberPasswordEncoder,
) {
    @Transactional
    fun signUp(request: SignUpRequest): AuthTokenResponse {
        val department = MemberDepartment.from(request.department!!)

        validateCollegeMatchesDepartment(MemberCollege.from(request.college!!), department)

        val member = Member.create(
            email = request.email!!,
            encodedPassword = passwordEncoder.encode(request.password!!),
            studentId = request.studentId!!,
            name = request.name!!,
            department = department,
            grade = MemberGrade.from(request.grade!!),
            academicStatus = AcademicStatus.from(request.academicStatus!!),
            lastSemesterGpa = request.lastSemesterGpa!!,
        )

        return jwtProvider.generateAuthToken(saveUniqueMember(member).id)
    }

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): AuthTokenResponse {
        val member = memberRepository.findByStudentId(request.studentId!!)
            ?: throw RestApiException(MEMBER_NOT_FOUND)

        if (!passwordEncoder.matches(request.password!!, member.password)) {
            throw RestApiException(PASSWORD_NOT_MATCH)
        }

        return jwtProvider.generateAuthToken(member.id)
    }

    @Transactional(readOnly = true)
    fun reissue(accessToken: String?): AuthTokenResponse {
        val memberId: Long = jwtProvider.getMemberIdAllowingExpiration(accessToken)

        if (!memberRepository.existsById(memberId)) {
            throw RestApiException(MEMBER_NOT_FOUND)
        }

        return jwtProvider.generateAuthToken(memberId)
    }

    @Transactional(readOnly = true)
    fun checkEmailAvailability(email: String): EmailAvailabilityResponse =
        EmailAvailabilityResponse.of(!memberRepository.existsByEmail(email))

    @Transactional(readOnly = true)
    fun checkStudentIdAvailability(studentId: String): StudentIdAvailabilityResponse =
        StudentIdAvailabilityResponse.of(!memberRepository.existsByStudentId(studentId))

    private fun validateCollegeMatchesDepartment(
        college: MemberCollege,
        department: MemberDepartment,
    ) {
        if (department.memberCollege != college) {
            throw RestApiException(COLLEGE_DEPARTMENT_MISMATCH)
        }
    }

    private fun saveUniqueMember(member: Member): Member {
        if (memberRepository.existsByStudentId(member.studentId)) {
            throw RestApiException(STUDENT_ID_ALREADY_EXISTS)
        }

        if (memberRepository.existsByEmail(member.email)) {
            throw RestApiException(EMAIL_ALREADY_EXISTS)
        }

        return try {
            memberRepository.saveAndFlush(member)
        } catch (e: DataIntegrityViolationException) {
            throw RestApiException(toDuplicateCode(e))
        }
    }

    private fun toDuplicateCode(exception: DataIntegrityViolationException): ExceptionCode {
        val cause = exception.cause

        if (cause is ConstraintViolationException &&
            STUDENT_ID_CONSTRAINT.equals(cause.constraintName, ignoreCase = true)
        ) {
            return STUDENT_ID_ALREADY_EXISTS
        }

        return EMAIL_ALREADY_EXISTS
    }

    companion object {
        private const val STUDENT_ID_CONSTRAINT = "uk_student_id"
    }
}
