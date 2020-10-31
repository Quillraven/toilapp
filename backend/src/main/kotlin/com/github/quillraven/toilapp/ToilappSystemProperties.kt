package com.github.quillraven.toilapp

object ToilappSystemProperties {
    fun isDevMode(): Boolean {
        return java.lang.Boolean.getBoolean("toilapp.devmode");
    }
}