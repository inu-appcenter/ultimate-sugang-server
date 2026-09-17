package uss.code.admin.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uss.code.admin.domain.SystemSemester
import uss.code.admin.dto.request.SystemSemesterRequest
import uss.code.admin.dto.response.SystemSemesterResponse
import uss.code.admin.repository.SystemSemesterRepository
import uss.code.global.exception.domain.ExceptionCode.SYSTEM_SEMESTER_NOT_FOUND
import uss.code.global.exception.domain.RestApiException

@Service
class SystemSemesterService(
    private val systemSemesterRepository: SystemSemesterRepository,
) {
    @Transactional(readOnly = true)
    fun getSystemSemester(): SystemSemesterResponse = SystemSemesterResponse.from(findSystemSemester())

    @Transactional
    fun changeSystemSemester(request: SystemSemesterRequest): SystemSemesterResponse {
        val systemSemester = findSystemSemester()

        systemSemester.change(request.academicYear!!, request.term!!)

        return SystemSemesterResponse.from(systemSemester)
    }

    private fun findSystemSemester(): SystemSemester =
        systemSemesterRepository.findAllOrdered().firstOrNull()
            ?: throw RestApiException(SYSTEM_SEMESTER_NOT_FOUND)
}
