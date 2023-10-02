@file:Suppress("HttpUrlsUsage", "unused")

package com.github.lamba92.aws.lambda.runtime

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

suspend inline fun <reified Input, reified Output> handleRequest(
    logger: RequestLogger<Input, Output>? = null,
    endpoints: AWSLambdaEndpoints = AWSLambdaEndpoints.v2018_06_01,
    host: String = AWS_LAMBDA_RUNTIME_API,
    client: HttpClient = AWS_HTTP_CLIENT,
    function: RequestContext<Input>.() -> Output
) {
    while (true) {
        val response = try {
            client.get("http://$host/${endpoints.version}/${endpoints.nextInvocation}")
        } catch (ex: Throwable) {
            val errorRequest = client.post("http://$host/${endpoints.version}/${endpoints.nextInvocationError}") {
                setError(ex)
            }
            val errorBody = runCatching { errorRequest.body<ErrorResponse>() }.getOrNull()
            logger?.onNextInvocationFailed(ex, errorBody)
            continue
        }
        val awsContext = response.getAWSHeaders()
        try {
            val body = response.body<Input>()
            logger?.onNextInvocationSuccessful(body, awsContext)
            val output = function(RequestContext(body, client, awsContext))
            client.post("http://$host/${endpoints.version}/${endpoints.response(awsContext.awsRequestId)}") {
                if (output != Unit && output != null) {
                    setBody(output)
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                }
            }
            logger?.onResponseSuccessful(output)
        } catch (ex: Throwable) {
            val errorRequest =
                client.post("http://$host/${endpoints.version}/${endpoints.responseError(awsContext.awsRequestId)}") {
                    setError(ex)
                }
            val errorBody = runCatching { errorRequest.body<ErrorResponse>() }.getOrNull()
            logger?.onResponseFailed(ex, errorBody)
        }
    }
}
