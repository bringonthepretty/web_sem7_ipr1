package com.wah.ipr1

import com.wah.ipr1.client.Client
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Sem7Ipr1Application

fun main(args: Array<String>) {
    runApplication<Sem7Ipr1Application>(*args)

    val session = Client
    session.console()
}
