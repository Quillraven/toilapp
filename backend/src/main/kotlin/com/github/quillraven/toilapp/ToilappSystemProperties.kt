package com.github.quillraven.toilapp

object ToilappSystemProperties {
    fun isDevMode() = "true" == System.getProperty("toilapp.devmode")
}
