package uss.code.member.dto.response;

import lombok.Builder;
import uss.code.member.domain.Member;

import static lombok.AccessLevel.PRIVATE;

@Builder(access = PRIVATE)
public record MemberProfileResponse(
        String email,

        String department,

        String studentId,

        String name,

        String grade,

        String academicStatus,

        double gpa,

        int creditLimit
) {
    public static MemberProfileResponse of(final Member member) {
        return MemberProfileResponse.builder()
                .email(member.getEmail())
                .department(member.getDepartment().getName())
                .studentId(member.getStudentId())
                .name(member.getName())
                .grade(member.getGrade().getName())
                .academicStatus(member.getAcademicStatus().getName())
                .gpa(member.getLastSemesterGpa())
                .creditLimit(member.getMaxCredit())
                .build();
    }
}
