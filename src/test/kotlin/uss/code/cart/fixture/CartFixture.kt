package uss.code.cart.fixture

import org.springframework.test.util.ReflectionTestUtils
import uss.code.cart.domain.Cart
import uss.code.course.domain.Course
import uss.code.member.domain.Member
import java.time.LocalDateTime

object CartFixture {
    fun createCart(
        member: Member,
        course: Course,
    ): Cart {
        return createCart(member, course, LocalDateTime.now())
    }

    fun createCart(
        member: Member,
        course: Course,
        createdAt: LocalDateTime,
    ): Cart {
        val cart = Cart.create(
            member = member,
            course = course,
        )
        ReflectionTestUtils.setField(cart, "createdAt", createdAt)

        increaseCartCount(course)

        return cart
    }

    private fun increaseCartCount(course: Course) {
        ReflectionTestUtils.setField(course, "cartCount", course.cartCount + 1)
    }
}
