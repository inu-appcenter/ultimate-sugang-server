package uss.code.admin.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uss.code.admin.dto.request.AdminLoginRequest
import uss.code.admin.dto.response.AdminTokenResponse
import uss.code.admin.infra.AdminPasswordEncoder
import uss.code.admin.repository.AdminRepository
import uss.code.auth.infra.JwtProvider
import uss.code.global.exception.domain.ExceptionCode.*
import uss.code.global.exception.domain.RestApiException

@Service
class AdminAuthService(
    private val adminRepository: AdminRepository,

    private val jwtProvider: JwtProvider,
    private val passwordEncoder: AdminPasswordEncoder,
) {
    @Transactional(readOnly = true)
    fun login(request: AdminLoginRequest): AdminTokenResponse {
        val admin = adminRepository.findByLoginId(request.loginId!!)
            ?: throw RestApiException(ADMIN_LOGIN_FAILED)

        if (!passwordEncoder.matches(request.password!!, admin.password)) {
            throw RestApiException(ADMIN_LOGIN_FAILED)
        }

        return AdminTokenResponse.of(
            accessToken = jwtProvider.generateAdminToken(admin.id),
            name = admin.name,
        )
    }

    @Transactional(readOnly = true)
    fun reissue(accessToken: String?): AdminTokenResponse {
        val adminId: Long = jwtProvider.getAdminIdAllowingExpiration(accessToken)

        if (!jwtProvider.isAdminToken(accessToken)) {
            throw RestApiException(ADMIN_ACCESS_DENIED)
        }

        val admin = adminRepository.findByIdOrNull(adminId)
            ?: throw RestApiException(ADMIN_NOT_FOUND)

        return AdminTokenResponse.of(
            accessToken = jwtProvider.generateAdminToken(admin.id),
            name = admin.name,
        )
    }
}
