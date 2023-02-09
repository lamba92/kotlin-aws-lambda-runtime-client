# Kotlin runtime client for AWS Lambda

Simple client intended for replacing the Runtime Interface Clients (RIC) for Java. 

The main advantage is to be reflection free to make it easier to compile a native image using GraalVM.  

## Usage

Artifact available at `com.github.lamba92:kotlin-aws-lambda-runtime-client:1.0.0`, see latest version for updates. 

Write you main function like this:

```kotlin
// com/github/lamba92/example/Main.kt
@Serializable
data class MyInput(val whatever: String)

@Serializable
data class MyOutput(val whateverOutput: Int)

suspend fun main() {
    handleRequest { input: MyInput, context: AWSContext -> MyOutput("Hello input: ${input.whatever}") }
}
```

Now use it as your main entry point in your custom runtime.
 - with the `application` Gradle plugin:
    ```kotlin
    application {
        mainClass.set("com.github.lamba92.example.MainKt")        
    }
    ```
 - with the GraalVM Native Image Gradle plugin:
   ```kotlin
   graalVm {
       binaries {
           create("myBinary") { 
               mainClass.set("com.github.lamba92.example.MainKt")
               classpath(sourceSets["main"].runtimeClasspath) // important!
           }        
       }
   }
   ```
   If yo ualso use the `application` plugin this configuration is not needed.
 - with the Package Search Native Runtime Gradle plugin:
   ```kotlin
   aws {
       lambdas {
           create("myLamda") { // or simply `main {
               entrypoint.set("com.github.lamba92.example.MainKt")
           }    
       }    
   }
   ```