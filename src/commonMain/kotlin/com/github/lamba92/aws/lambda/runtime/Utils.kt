package com.github.lamba92.aws.lambda.runtime

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit
import kotlin.time.toDuration

data class RequestContext<Input>(val input: Input, val client: HttpClient, val awsContext: AWSContext)

@Serializable
data class ErrorResponse(
    val errorMessage: String,
    val errorType: String,
    val stackStrace: List<String> = emptyList()
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
    header(HttpHeaders.LambdaRuntimeFunctionErrorType, "Runtime.${ex::class.simpleName}")
    header(HttpHeaders.ContentType, ContentType.Application.Json)
    setBody(ex.asErrorResponse())
}

fun HttpResponse.getAWSHeaders() = AWSContext(
    headers[HttpHeaders.LambdaRuntimeRequestId]
        ?: error("Header ${HttpHeaders.LambdaRuntimeRequestId} not found."),
    headers[HttpHeaders.LambdaRuntimeDeadlineMs]?.toLong()
        ?: error("Header ${HttpHeaders.LambdaRuntimeDeadlineMs} not found."),
    headers[HttpHeaders.LambdaRuntimeInvokedFunctionArn]
        ?: error("Header ${HttpHeaders.LambdaRuntimeInvokedFunctionArn} not found."),
    headers[HttpHeaders.LambdaRuntimeTraceId]
)

var HttpTimeout.HttpTimeoutCapabilityConfiguration.requestTimeout: kotlin.time.Duration?
    get() = requestTimeoutMillis?.toDuration(DurationUnit.MILLISECONDS)
    set(value) {
        requestTimeoutMillis = value?.inWholeMilliseconds
    }

val AWS_LAMBDA_RUNTIME_API: String by EnvironmentProvider

expect object EnvironmentProvider : ReadOnlyProperty<Any?, String> {
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): String
}

val AWS_HTTP_CLIENT by lazy {
    HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
            })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
        install(HttpTimeout) {
            requestTimeout = 15.minutes
        }
    }
}