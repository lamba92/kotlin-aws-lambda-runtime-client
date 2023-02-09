package com.github.lamba92.aws.lambda.runtime

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit
import kotlin.time.toDuration


@Serializable
data class ErrorResponse(
    val errorMessage: String,
    val errorType: String,
    val stackStrace: List<String>
)

@Serializable
data class AWSContext(
    val awsRequestId: String,
    val runtimeDeadlineMs: Long,
    val invokedFunctionArn: String,
    val traceId: String?
)

fun Throwable.asErrorResponse() =
    ErrorResponse(message ?: "", this::class.simpleName!!, stackTraceToString().lines())

fun HttpRequestBuilder.setError(ex: Throwable) {
    header(HttpHeaders.`Lambda-Runtime-Function-Error-Type`, "Runtime.${ex::class.simpleName}")
    setBody(ex.asErrorResponse())
}

fun HttpResponse.getAWSHeaders() = AWSContext(
    headers[HttpHeaders.`Lambda-Runtime-Aws-Request-Id`]
        ?: error("Header ${HttpHeaders.`Lambda-Runtime-Aws-Request-Id`} not found."),
    headers[HttpHeaders.`Lambda-Runtime-Deadline-Ms`]?.toLong()
        ?: error("Header ${HttpHeaders.`Lambda-Runtime-Deadline-Ms`} not found."),
    headers[HttpHeaders.`Lambda-Runtime-Invoked-Function-Arn`]
        ?: error("Header ${HttpHeaders.`Lambda-Runtime-Invoked-Function-Arn`} not found."),
    headers[HttpHeaders.`Lambda-Runtime-Trace-Id`]
)

var HttpTimeout.HttpTimeoutCapabilityConfiguration.requestTimeout: kotlin.time.Duration?
    get() = requestTimeoutMillis?.toDuration(DurationUnit.MILLISECONDS)
    set(value) {
        requestTimeoutMillis = value?.inWholeMilliseconds
    }

val AWS_LAMBDA_RUNTIME_API: String by System.getenv()

val AWS_HTTP_CLIENT = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.ALL
    }
    install(HttpTimeout) {
        requestTimeout = 15.minutes
    }
}