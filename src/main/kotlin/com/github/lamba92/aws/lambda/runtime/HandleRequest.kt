@file:Suppress("HttpUrlsUsage", "unused")

package com.github.lamba92.aws.lambda.runtime

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

suspend inline fun <reified Input, reified Output> handleRequest(
    logger: RequestLogger<Input, Output>? = null,
    endpoints: AWSLambdaEndpoints = AWSLambdaEndpoints.`2018-06-01`,
    host: String = AWS_LAMBDA_RUNTIME_API,
    client: HttpClient = AWS_HTTP_CLIENT,
    function: (Input, AWSContext) -> Output
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
            val output = function(body, awsContext)
            client.post("http://$host/${endpoints.version}/${endpoints.response(awsContext.awsRequestId)}") {
                setBody(output)
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
