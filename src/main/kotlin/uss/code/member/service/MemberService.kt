package uss.code.member.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uss.code.global.exception.domain.ExceptionCode.MEMBER_NOT_FOUND
import uss.code.global.exception.domain.RestApiException
import uss.code.member.domain.MemberDepartment
import uss.code.member.dto.request.DepartmentUpdateRequest
import uss.code.member.dto.response.MemberProfileResponse
import uss.code.member.repository.MemberRepository

@Service
class MemberService(
    private val memberRepository: MemberRepository,
) {
    @Transactional(readOnly = true)
    fun getProfile(memberId: Long): MemberProfileResponse {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: throw RestApiException(MEMBER_NOT_FOUND)

        return MemberProfileResponse.of(member)
    }

    @Transactional
    fun updateDepartment(
        memberId: Long,
        request: DepartmentUpdateRequest,
    ) {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: throw RestApiException(MEMBER_NOT_FOUND)

        member.updateDepartment(MemberDepartment.from(request.department!!))
    }
}
