import com.moowork.gradle.node.npm.NpmTask

plugins {
    id("com.moowork.node") version "1.3.1"
    base
}

node {
    version = "12.18.3"
    npmVersion = "6.14.7"
    download = true
}

val bundle by tasks.registering(NpmTask::class) {
    setArgs(listOf("run", "build"))
}

tasks.assemble {
    dependsOn(bundle)
}
