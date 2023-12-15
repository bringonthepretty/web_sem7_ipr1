package com.wah.ipr1.client

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate

/**
 * This object represents client application
 */
object Client {

    private val xmlBody = """
    <websites>
        <website url="https://whatever.com">
            <name>Baeldung</name>
            <category>Online Courses</category>
            <status>Online</status>
        </website>
        <website url="http://example.com">
            <name>Example</name>
            <category>Examples</category>
            <status>Offline</status>
        </website>
        <website url="http://localhost:8080">
            <name>Localhost</name>
            <category>Tests</category>
            <status>Offline</status>
        </website>
    </websites>""".trimMargin()

    /**
     * Invokes console logic
     */
    fun console() {
        var flag = true

        while (flag) {
            println("""Chose parse type
            |1. SAX
            |2. StAX
            |3. DOM
            |4. Exit (default)
        """.trimMargin())

            val type = when (readln()) {
                "1" -> {
                    "sax"
                }
                "2" -> {
                    "stax"
                }
                "3" -> {
                    "dom"
                }
                else -> {
                    flag = false
                    null
                }
            }

            type?.let {
                makeRequest(it)?.let { body ->
                    println(body)
                }
            }
        }
    }

    private fun makeRequest(type: String): String? {
        val restTemplate = RestTemplate()

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val httpEntity = HttpEntity<Any>(xmlBody, headers)

        val result = restTemplate.exchange(
            "http://localhost:8080/parse?type={type}",
            HttpMethod.POST,
            httpEntity,
            String::class.java,
            type
        )
        return result.body
    }

}