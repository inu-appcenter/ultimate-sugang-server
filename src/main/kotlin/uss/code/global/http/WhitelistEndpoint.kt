package uss.code.global.http

import org.springframework.http.HttpMethod

object WhitelistEndpoint {
    private const val PATH_WILDCARD_SUFFIX = "/**"
    private const val PATH_DELIMITER = "/"

    private val WHITELIST = listOf(
        EndPoint(
            path = "/api/v1/auth/login",
            httpMethod = HttpMethod.POST,
        ),
        EndPoint(
            path = "/api/v1/auth/sign-up",
            httpMethod = HttpMethod.POST,
        ),
        EndPoint(
            path = "/api/v1/auth/email-availability",
            httpMethod = HttpMethod.GET,
        ),
        EndPoint(
            path = "/api/v1/auth/student-id-availability",
            httpMethod = HttpMethod.GET,
        ),
        EndPoint(
            path = "/api/v1/auth/re-issue",
            httpMethod = HttpMethod.POST,
        ),
        EndPoint(
            path = "/actuator/**",
            httpMethod = null,
        ),
        EndPoint(
            path = "/swagger-ui/**",
            httpMethod = null,
        ),
        EndPoint(
            path = "/v3/api-docs/**",
            httpMethod = null,
        ),
        EndPoint(
            path = "/swagger-resources/**",
            httpMethod = null,
        ),
    )

    fun isWhitelisted(
        path: String,
        method: String,
    ): Boolean {
        return WHITELIST.any { it.matches(path, method) }
    }

    private data class EndPoint(
        val path: String,
        val httpMethod: HttpMethod?,
    ) {
        fun matches(
            uri: String,
            method: String,
        ): Boolean {
            val pathMatches = isPathMatch(uri)
            val methodMatches = httpMethod == null || httpMethod.name().equals(method, ignoreCase = true)
            return pathMatches && methodMatches
        }

        private fun isPathMatch(uri: String): Boolean {
            if (!path.endsWith(PATH_WILDCARD_SUFFIX)) {
                return path == uri
            }

            val basePath = path.removeSuffix(PATH_WILDCARD_SUFFIX)

            return uri == basePath || uri.startsWith("$basePath$PATH_DELIMITER")
        }
    }
}
