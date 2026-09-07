package uss.code.course.dto.common;

public record CourseCapacity(
        long id,

        int currentEnrollment,

        int maxCapacity,

        int cartCount
) {
    public boolean isRegisterable() {
        return currentEnrollment < maxCapacity;
    }
}
