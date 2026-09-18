package uss.code.course.infra

object SearchKeywordSanitizer {
    private val BOOLEAN_OPERATORS = Regex("""[+\-<>()~*"@]""")
    private val WHITESPACES = Regex("""\s+""")
    private const val SPACE = " "

    fun sanitize(keyword: String): String {
        val withoutOperators = BOOLEAN_OPERATORS.replace(keyword, SPACE)

        return WHITESPACES.replace(withoutOperators, SPACE).trim { it <= ' ' }
    }
}
