package uss.code.cart.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uss.code.cart.domain.Cart
import uss.code.cart.repository.CartRepository
import uss.code.course.domain.Course
import uss.code.course.dto.response.CourseResponse
import uss.code.course.dto.response.CoursesResponse
import uss.code.course.infra.CourseValidator
import uss.code.course.repository.CourseRepository
import uss.code.global.exception.domain.ExceptionCode.CARTED_COURSE_DELETE_CONFLICT
import uss.code.global.exception.domain.ExceptionCode.CARTED_COURSE_LIMIT_EXCEEDED
import uss.code.global.exception.domain.ExceptionCode.CARTED_COURSE_NOT_FOUND
import uss.code.global.exception.domain.ExceptionCode.COURSE_ALREADY_IN_CART
import uss.code.global.exception.domain.ExceptionCode.COURSE_CLOSED
import uss.code.global.exception.domain.ExceptionCode.COURSE_NOT_FOUND
import uss.code.global.exception.domain.ExceptionCode.COURSE_SCHEDULE_CONFLICT
import uss.code.global.exception.domain.ExceptionCode.COURSE_TYPE_LIMIT_EXCEEDED
import uss.code.global.exception.domain.ExceptionCode.MEMBER_NOT_FOUND
import uss.code.global.exception.domain.RestApiException
import uss.code.member.repository.MemberRepository

@Service
class CartService(
    private val cartRepository: CartRepository,
    private val courseRepository: CourseRepository,
    private val memberRepository: MemberRepository,
) {
    @Transactional(readOnly = true)
    fun getCartedCourse(memberId: Long): CoursesResponse {
        val carts = cartRepository.findByMemberId(memberId)

        val courseResponses = carts.map { CourseResponse.from(it.course) }

        return CoursesResponse.of(courseResponses)
    }

    @Transactional
    fun addCart(
        memberId: Long,
        courseId: Long,
    ) {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: throw RestApiException(MEMBER_NOT_FOUND)

        val carts = cartRepository.findByMemberId(memberId)

        val course = courseRepository.findByIdWithSchedules(courseId)
            ?: throw RestApiException(COURSE_NOT_FOUND)

        validateCourseActive(course)
        validateCartLimit(carts)
        validateDuplicateCourse(carts, courseId)
        validateCourseScheduleConflict(carts, course)
        validateCourseTypeLimit(carts, course)

        courseRepository.increaseCartCount(courseId)

        val cart = Cart.create(member, course)

        cartRepository.save(cart)
    }

    @Transactional
    fun deleteCartedCourse(
        memberId: Long,
        courseId: Long,
    ) {
        val cart = cartRepository.findByMemberIdAndCourseId(memberId, courseId)
            ?: throw RestApiException(CARTED_COURSE_NOT_FOUND)

        decreaseCartCount(courseId)

        cartRepository.delete(cart)
    }

    private fun decreaseCartCount(courseId: Long) {
        val affectedRows = courseRepository.decreaseCartCountAboveZero(courseId)

        if (affectedRows == NO_AFFECTED_ROW) {
            throw RestApiException(CARTED_COURSE_DELETE_CONFLICT)
        }
    }

    private fun validateCourseActive(course: Course) {
        if (!course.isActive()) {
            throw RestApiException(COURSE_CLOSED)
        }
    }

    private fun validateCartLimit(carts: List<Cart>) {
        if (carts.size >= MAX_CART_COUNT) {
            throw RestApiException(CARTED_COURSE_LIMIT_EXCEEDED)
        }
    }

    private fun validateCourseScheduleConflict(
        carts: List<Cart>,
        course: Course,
    ) {
        val cartedCourses = carts.map { it.course }

        if (!CourseValidator.validateCourseScheduleNotConflict(cartedCourses, course)) {
            throw RestApiException(COURSE_SCHEDULE_CONFLICT)
        }
    }

    private fun validateCourseTypeLimit(
        carts: List<Cart>,
        course: Course,
    ) {
        val cartedCourses = carts.map { it.course }

        if (!CourseValidator.validateCourseTypeLimit(cartedCourses, course)) {
            throw RestApiException(COURSE_TYPE_LIMIT_EXCEEDED)
        }
    }

    private fun validateDuplicateCourse(
        carts: List<Cart>,
        courseId: Long,
    ) {
        val exists = carts.any { it.course.id == courseId }

        if (exists) {
            throw RestApiException(COURSE_ALREADY_IN_CART)
        }
    }

    companion object {
        private const val NO_AFFECTED_ROW = 0
        private const val MAX_CART_COUNT = 10
    }
}
