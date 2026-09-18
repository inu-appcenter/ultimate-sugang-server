package uss.code.global.http

import org.springframework.http.HttpMethod

object AdminEndpoint {
    private const val ADMIN_BASE_PATH = "/api/v1/admin"
    private const val PATH_DELIMITER = "/"

    private val ADMIN_WHITELIST = listOf(
        EndPoint(
            path = "/api/v1/admin/auth/login",
            httpMethod = HttpMethod.POST,
        ),
        EndPoint(
            path = "/api/v1/admin/auth/refresh",
            httpMethod = HttpMethod.POST,
        ),
    )

    fun isAdminPath(uri: String): Boolean {
        return uri == ADMIN_BASE_PATH || uri.startsWith("$ADMIN_BASE_PATH$PATH_DELIMITER")
    }

    fun isWhitelisted(
        path: String,
        method: String,
    ): Boolean {
        return ADMIN_WHITELIST.any { it.matches(path, method) }
    }

    private data class EndPoint(
        val path: String,
        val httpMethod: HttpMethod,
    ) {
        fun matches(
            uri: String,
            method: String,
        ): Boolean {
            return path == uri && httpMethod.name().equals(method, ignoreCase = true)
        }
    }
}
