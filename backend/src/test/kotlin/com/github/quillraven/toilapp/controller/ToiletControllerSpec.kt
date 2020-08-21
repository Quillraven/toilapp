package com.github.quillraven.toilapp.controller

import ModelWithFields
import ModelWithNoFields
import org.amshove.kluent.`should be equal to`
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

@Suppress("unused")
object ToiletControllerSpec : Spek({
    describe("A ToiletController") {
        describe("Retrieving non-null fields of a class with zero null non-null fields") {
            it("should return an empty array") {
                ModelWithNoFields()::class.getNonNullProperties() `should be equal to` arrayOf()
            }
        }

        describe("Retrieving non-null fields of a class with a non-null field") {
            it("should return an array containing the name of the field") {
                val expected = ModelWithFields()::class.getNonNullProperties()

                expected `should be equal to` arrayOf(ModelWithFields::field1.name, ModelWithFields::field2.name)
            }
        }

        describe("Retrieving non-null fields of a class with a non-null field using exceptions") {
            it("should return an array containing the name of the field") {
                val expected = ModelWithFields()::class.getNonNullProperties(ModelWithFields::field2)

                expected `should be equal to` arrayOf("field1")
            }
        }
    }
})
