package uss.code.registration.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uss.code.course.domain.Course;
import uss.code.course.infra.CourseValidator;
import uss.code.course.repository.CourseRepository;
import uss.code.global.exception.domain.RestApiException;
import uss.code.member.domain.Member;
import uss.code.member.repository.MemberRepository;
import uss.code.registration.domain.Registration;
import uss.code.registration.dto.response.RegistrationCourseResponse;
import uss.code.registration.dto.response.RegistrationCoursesResponse;
import uss.code.registration.dto.response.RegistrationResponse;
import uss.code.registration.infra.RegistrationTypeResolver;
import uss.code.registration.repository.RegistrationRepository;

import java.util.List;

import static uss.code.global.exception.domain.ExceptionCode.*;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private static final int NO_AFFECTED_ROW = 0;

    private final RegistrationRepository registrationRepository;
    private final MemberRepository memberRepository;
    private final CourseRepository courseRepository;

    @Transactional(readOnly = true)
    public RegistrationCoursesResponse getRegistrationCourse(final long memberId) {
        final Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RestApiException(MEMBER_NOT_FOUND));

        final List<Registration> registrations = registrationRepository.findByMemberId(memberId);

        final List<RegistrationCourseResponse> registrationCourseResponses = registrations.stream()
                .map(registration -> RegistrationCourseResponse.of(
                        registration,
                        member.getStudentId(),
                        RegistrationTypeResolver.resolve(member, registration.getCourse())
                ))
                .toList();

        return RegistrationCoursesResponse.of(registrationCourseResponses);
    }

    @Transactional
    public RegistrationResponse registerCourse(
            final long memberId,
            final long courseId
    ) {
        final Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RestApiException(MEMBER_NOT_FOUND));

        final List<Registration> registrations = registrationRepository.findByMemberId(memberId);

        final Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RestApiException(COURSE_NOT_FOUND));

        validateCourseActive(course);
        validateCourseScheduleConflict(registrations, course);
        validateDuplicateSubject(registrations, course);
        validateDuplicateCourse(registrations, courseId);
        validateCreditLimit(registrations, member, course);
        validateCourseTypeLimit(registrations, course);

        increaseEnrollment(courseId);

        final Registration registration = Registration.create(member, course);

        registrationRepository.save(registration);

        return RegistrationResponse.from(course);
    }

    @Transactional
    public void deleteRegisteredCourse(
            final long memberId,
            final long courseId
    ) {
        final Registration registration = registrationRepository.findByMemberIdAndCourseId(memberId, courseId)
                .orElseThrow(() -> new RestApiException(REGISTERED_COURSE_NOT_FOUND));

        decreaseEnrollment(courseId);

        registrationRepository.delete(registration);
    }

    private void validateCreditLimit(
            final List<Registration> registrations,
            final Member member,
            final Course course
    ) {
        if (!CourseValidator.validateCreditLimit(registrations, member, course)) {
            throw new RestApiException(CREDIT_LIMIT_EXCEEDED);
        }
    }

    private void validateCourseActive(final Course course) {
        if (!course.isActive()) {
            throw new RestApiException(COURSE_CLOSED);
        }
    }

    private void increaseEnrollment(final long courseId) {
        final int affectedRows = courseRepository.increaseEnrollmentWithinCapacity(courseId);

        if (affectedRows == NO_AFFECTED_ROW) {
            throw new RestApiException(COURSE_MAX_CAPACITY_EXCEEDED);
        }
    }

    private void decreaseEnrollment(final long courseId) {
        final int affectedRows = courseRepository.decreaseEnrollmentAboveZero(courseId);

        if (affectedRows == NO_AFFECTED_ROW) {
            throw new RestApiException(REGISTRATION_CANCEL_CONFLICT);
        }
    }

    private void validateDuplicateCourse(
            final List<Registration> registrations,
            final long courseId
    ) {
        boolean exists = registrations.stream()
                .anyMatch(registration -> registration.getCourse().getId().equals(courseId));

        if (exists) {
            throw new RestApiException(COURSE_ALREADY_REGISTERED);
        }
    }

    private void validateDuplicateSubject(
            final List<Registration> registrations,
            final Course course
    ) {
        boolean exists = registrations.stream()
                .anyMatch(registration -> registration.getCourse().getTitleKr().equals(course.getTitleKr()));

        if (exists) {
            throw new RestApiException(DUPLICATE_SUBJECT_REGISTERED);
        }
    }

    private void validateCourseScheduleConflict(
            final List<Registration> registrations,
            final Course course
    ) {
        final List<Course> registeredCourses = registrations.stream()
                .map(Registration::getCourse)
                .toList();

        if (!CourseValidator.validateCourseScheduleNotConflict(registeredCourses, course)) {
            throw new RestApiException(COURSE_SCHEDULE_CONFLICT);
        }
    }

    private void validateCourseTypeLimit(
            final List<Registration> registrations,
            final Course course
    ) {
        final List<Course> registeredCourses = registrations.stream()
                .map(Registration::getCourse)
                .toList();

        if (!CourseValidator.validateCourseTypeLimit(registeredCourses, course)) {
            throw new RestApiException(COURSE_TYPE_LIMIT_EXCEEDED);
        }
    }
}
