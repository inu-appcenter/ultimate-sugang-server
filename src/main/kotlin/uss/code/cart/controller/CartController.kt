package uss.code.cart.controller

import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import uss.code.auth.annotation.Auth
import uss.code.cart.service.CartService
import uss.code.course.dto.response.CoursesResponse

@RestController
@RequestMapping("/api/v1/carts")
class CartController(
    private val cartService: CartService,
) : CartControllerDocs {

    @GetMapping
    override fun getCartedCourse(
        @Auth memberId: Long,
    ): ResponseEntity<CoursesResponse> {
        val response = cartService.getCartedCourse(memberId)
        return ResponseEntity.status(OK).body(response)
    }

    @PostMapping("/{course-id}")
    override fun addCart(
        @Auth memberId: Long,
        @PathVariable("course-id") courseId: Long,
    ): ResponseEntity<Void> {
        cartService.addCart(memberId, courseId)
        return ResponseEntity.status(OK).build()
    }

    @DeleteMapping("/{course-id}")
    override fun deleteCartedCourse(
        @Auth memberId: Long,
        @PathVariable("course-id") courseId: Long,
    ): ResponseEntity<Void> {
        cartService.deleteCartedCourse(memberId, courseId)
        return ResponseEntity.status(NO_CONTENT).build()
    }
}
