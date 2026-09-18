package uss.code

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class UssServerApplication

fun main(args: Array<String>) {
    runApplication<UssServerApplication>(*args)
}
