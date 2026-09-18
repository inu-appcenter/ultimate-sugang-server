package uss.code.global.config

import com.p6spy.engine.logging.Category
import com.p6spy.engine.spy.appender.MessageFormattingStrategy
import org.hibernate.engine.jdbc.internal.FormatStyle

class P6SpySqlFormatter : MessageFormattingStrategy {
    override fun formatMessage(
        connectionId: Int,
        now: String,
        elapsed: Long,
        category: String,
        prepared: String?,
        sql: String?,
        url: String?,
    ): String {
        if (sql == null || sql.trim { it <= ' ' }.isEmpty()) {
            return ""
        }

        val formattedSql = formatSql(category, sql)

        return "\n[P6Spy - ${elapsed}ms]\n$formattedSql\n"
    }

    private fun formatSql(
        category: String,
        sql: String,
    ): String {
        if (Category.STATEMENT.name == category) {
            val lowerSql = sql.trim { it <= ' ' }.lowercase()

            if (lowerSql.startsWith("create") ||
                lowerSql.startsWith("alter") ||
                lowerSql.startsWith("comment")
            ) {
                return FormatStyle.DDL.formatter.format(sql)
            }
            return FormatStyle.BASIC.formatter.format(sql)
        }
        return sql
    }
}
