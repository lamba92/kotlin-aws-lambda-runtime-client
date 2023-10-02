@file:Suppress("ObjectPropertyName", "UnusedReceiverParameter")

package com.github.lamba92.aws.lambda.runtime

import io.ktor.http.*

val HttpHeaders.LambdaRuntimeRequestId
    get() = "Lambda-Runtime-Aws-Request-Id"

val HttpHeaders.LambdaRuntimeDeadlineMs
    get() = "Lambda-Runtime-Deadline-Ms"

val HttpHeaders.LambdaRuntimeInvokedFunctionArn
    get() = "Lambda-Runtime-Deadline-Ms"

val HttpHeaders.LambdaRuntimeTraceId
    get() = "Lambda-Runtime-Trace-Id"

val HttpHeaders.LambdaRuntimeFunctionErrorType
    get() = "Lambda-Runtime-Function-Error-Type"