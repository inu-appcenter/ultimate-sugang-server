package uss.code.course.infra;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public class SearchKeywordSanitizer {

    private static final Pattern BOOLEAN_OPERATORS = Pattern.compile("[+\\-<>()~*\"@]");
    private static final Pattern WHITESPACES = Pattern.compile("\\s+");
    private static final String SPACE = " ";

    public static String sanitize(final String keyword) {
        final String withoutOperators = BOOLEAN_OPERATORS.matcher(keyword).replaceAll(SPACE);

        return WHITESPACES.matcher(withoutOperators).replaceAll(SPACE).trim();
    }
}
