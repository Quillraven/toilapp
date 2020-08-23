package com.github.quillraven.toilapp.model

class ModelWithNoFields

data class ModelWithFields(
    val field1: String = "",
    val field2: String = "",
    val field3: String? = null
)
