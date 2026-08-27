package indi.kyson.laocai

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class LaocaiApplication

fun main(args: Array<String>) {
    runApplication<LaocaiApplication>(*args)
}
