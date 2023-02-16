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
    endpoints: AWSLambdaEndpoints = AWSLambdaEndpoints.`2018-06-01`,
    host: String = AWS_LAMBDA_RUNTIME_API,
    client: HttpClient = AWS_HTTP_CLIENT,
    function: RequestContext<Input>.() -> Output
) {
    while (true) {
        val (body, awsContext) = try {
            val r = client.get("http://$host/${endpoints.version}/${endpoints.nextInvocation}")
            val body = r.body<Input>()
            val awsContext = r.getAWSHeaders()
            logger?.onNextInvocationSuccessful(body, awsContext)
            body to awsContext
        } catch (ex: Throwable) {
            client.post("http://$host/${endpoints.version}/${endpoints.nextInvocationError}") {
                setError(ex)
            }
            logger?.onNextInvocationFailed(ex)
            continue
        }

        try {
            val output = function(RequestContext(body, client, awsContext))
            client.post("http://$host/${endpoints.version}/${endpoints.response(awsContext.awsRequestId)}") {
                if (output != Unit && output != null) {
                    setBody(output)
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                }
            }
            logger?.onResponseSuccessful(output)
        } catch (ex: Throwable) {
            client.post("http://$host/${endpoints.version}/${endpoints.responseError(awsContext.awsRequestId)}") {
                setError(ex)
            }
            logger?.onResponseFailed(ex)
        }
    }
}
