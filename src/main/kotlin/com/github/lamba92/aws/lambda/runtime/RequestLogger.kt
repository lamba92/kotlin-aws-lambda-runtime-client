package com.github.lamba92.aws.lambda.runtime

interface RequestLogger<Input, Output> {

    fun onNextInvocationFailed(ex: Throwable)
    fun onNextInvocationSuccessful(input: Input, context: AWSContext) {}

    fun onResponseSuccessful(output: Output) {}
    fun onResponseFailed(ex: Throwable)

}