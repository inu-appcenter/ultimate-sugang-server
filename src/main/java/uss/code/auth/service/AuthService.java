package uss.code.auth.service;

import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uss.code.auth.dto.request.LoginRequest;
import uss.code.auth.dto.request.SignUpRequest;
import uss.code.auth.dto.response.AuthTokenResponse;
import uss.code.auth.dto.response.EmailAvailabilityResponse;
import uss.code.auth.dto.response.StudentIdAvailabilityResponse;
import uss.code.auth.infra.JwtProvider;
import uss.code.auth.infra.MemberPasswordEncoder;
import uss.code.global.exception.domain.ExceptionCode;
import uss.code.global.exception.domain.RestApiException;
import uss.code.member.domain.AcademicStatus;
import uss.code.member.domain.Member;
import uss.code.member.domain.MemberCollege;
import uss.code.member.domain.MemberDepartment;
import uss.code.member.domain.MemberGrade;
import uss.code.member.repository.MemberRepository;

import static uss.code.global.exception.domain.ExceptionCode.COLLEGE_DEPARTMENT_MISMATCH;
import static uss.code.global.exception.domain.ExceptionCode.EMAIL_ALREADY_EXISTS;
import static uss.code.global.exception.domain.ExceptionCode.MEMBER_NOT_FOUND;
import static uss.code.global.exception.domain.ExceptionCode.PASSWORD_NOT_MATCH;
import static uss.code.global.exception.domain.ExceptionCode.STUDENT_ID_ALREADY_EXISTS;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String STUDENT_ID_CONSTRAINT = "uk_student_id";

    private final JwtProvider jwtProvider;
    private final MemberPasswordEncoder passwordEncoder;

    private final MemberRepository memberRepository;

    @Transactional
    public AuthTokenResponse signUp(final SignUpRequest request) {
        final MemberDepartment department = MemberDepartment.from(request.department());

        validateCollegeMatchesDepartment(MemberCollege.from(request.college()), department);

        final Member member = Member.create(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.studentId(),
                request.name(),
                department,
                MemberGrade.from(request.grade()),
                AcademicStatus.from(request.academicStatus()),
                request.lastSemesterGpa()
        );

        return jwtProvider.generateAuthToken(saveUniqueMember(member).getId());
    }

    @Transactional(readOnly = true)
    public EmailAvailabilityResponse checkEmailAvailability(final String email) {
        return EmailAvailabilityResponse.of(!memberRepository.existsByEmail(email));
    }

    @Transactional(readOnly = true)
    public StudentIdAvailabilityResponse checkStudentIdAvailability(final String studentId) {
        return StudentIdAvailabilityResponse.of(!memberRepository.existsByStudentId(studentId));
    }

    @Transactional(readOnly = true)
    public AuthTokenResponse login(final LoginRequest request) {
        final Member member = memberRepository.findByStudentId(request.studentId())
                .orElseThrow(() -> new RestApiException(MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), member.getPassword()))
            throw new RestApiException(PASSWORD_NOT_MATCH);

        return jwtProvider.generateAuthToken(member.getId());
    }

    @Transactional(readOnly = true)
    public AuthTokenResponse reIssue(final String accessToken) {
        final Long memberId = jwtProvider.getMemberIdAllowingExpiration(accessToken);

        if (!memberRepository.existsById(memberId))
            throw new RestApiException(MEMBER_NOT_FOUND);

        return jwtProvider.generateAuthToken(memberId);
    }

    private void validateCollegeMatchesDepartment(
            final MemberCollege college,
            final MemberDepartment department
    ) {
        if (department.getMemberCollege() != college) {
            throw new RestApiException(COLLEGE_DEPARTMENT_MISMATCH);
        }
    }

    private Member saveUniqueMember(final Member member) {
        if (memberRepository.existsByStudentId(member.getStudentId())) {
            throw new RestApiException(STUDENT_ID_ALREADY_EXISTS);
        }

        if (memberRepository.existsByEmail(member.getEmail())) {
            throw new RestApiException(EMAIL_ALREADY_EXISTS);
        }

        try {
            return memberRepository.saveAndFlush(member);
        } catch (final DataIntegrityViolationException e) {
            throw new RestApiException(toDuplicateCode(e));
        }
    }

    private ExceptionCode toDuplicateCode(final DataIntegrityViolationException exception) {
        if (exception.getCause() instanceof ConstraintViolationException violation
                && STUDENT_ID_CONSTRAINT.equalsIgnoreCase(violation.getConstraintName())) {
            return STUDENT_ID_ALREADY_EXISTS;
        }

        return EMAIL_ALREADY_EXISTS;
    }
}
