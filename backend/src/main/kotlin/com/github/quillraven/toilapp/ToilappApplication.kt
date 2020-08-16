package com.github.quillraven.toilapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ToilappApplication

fun main(args: Array<String>) {
	runApplication<ToilappApplication>(*args)
}
