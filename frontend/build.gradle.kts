import com.moowork.gradle.node.npm.NpmTask

plugins {
    id("com.github.node-gradle.node") version "2.2.4"
}

node {
    download = true
    version = "12.18.3"
}

tasks.register<NpmTask>("npmStart") {
    group = "node"
    setArgs(listOf("start"))
}
