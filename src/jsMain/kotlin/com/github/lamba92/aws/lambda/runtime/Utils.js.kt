package com.github.lamba92.aws.lambda.runtime

import NodeJS.get
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import process


actual object EnvironmentProvider : ReadOnlyProperty<Any?, String> {
    actual override operator fun getValue(thisRef: Any?, property: KProperty<*>): String =
        process.env[property.name] ?: error("'${property.name}' not found in environment")
}