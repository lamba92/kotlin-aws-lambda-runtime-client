@file:OptIn(ExperimentalForeignApi::class)

package com.github.lamba92.aws.lambda.runtime

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

actual object EnvironmentProvider : ReadOnlyProperty<Any?, String> {
    actual override operator fun getValue(thisRef: Any?, property: KProperty<*>): String =
        platform.posix.getenv(property.name)?.toKString()
            ?: error("'${property.name}' not found in environment")
}