@file:Suppress("ObjectPropertyName")

package com.github.lamba92.aws.lambda.runtime

interface AWSLambdaEndpoints {

    companion object {
        val `2018-06-01`= object : AWSLambdaEndpoints {
            override val version = "2018-06-01"
            override val nextInvocation= "runtime/invocation/next"
            override val nextInvocationError = "runtime/init/error"
            override fun response(requestId: String) = "runtime/invocation/$requestId/response"
            override fun responseError(requestId: String) = "runtime/invocation/$requestId/error"
        }
    }

    val version: String
    val nextInvocation: String
    val nextInvocationError: String
    fun response(requestId: String): String
    fun responseError(requestId: String): String
}