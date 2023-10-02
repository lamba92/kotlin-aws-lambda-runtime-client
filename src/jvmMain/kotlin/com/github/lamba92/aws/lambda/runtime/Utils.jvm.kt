package com.github.lamba92.aws.lambda.runtime

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

actual object EnvironmentProvider : ReadOnlyProperty<Any?, String> {
    actual override operator fun getValue(thisRef: Any?, property: KProperty<*>): String =
        System.getenv(property.name) ?: error("'${property.name}' not found in environment")
}