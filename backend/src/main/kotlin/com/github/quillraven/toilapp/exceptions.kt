package com.github.quillraven.toilapp

import org.slf4j.LoggerFactory
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.reactive.function.server.ServerRequest

@Component
class GlobalErrorAttributes : DefaultErrorAttributes() {
    override fun getErrorAttributes(request: ServerRequest?, options: ErrorAttributeOptions?): MutableMap<String, Any> {
        return super.getErrorAttributes(request, options).apply {
            val error = getError(request)
            if (error is ToilappException) {
                LOG.error("ToilappException occurred", error)
                this.replace("status", error.statusCode.value())
                this.replace("error", error.statusCode.reasonPhrase)
                this.replace("message", error.statusText)
            }
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(GlobalErrorAttributes::class.java)
    }
}

abstract class ToilappException(statusCode: HttpStatus, statusText: String) :
    HttpStatusCodeException(statusCode, statusText)

class ToiletDoesNotExistException(id: String) :
    ToilappException(HttpStatus.NOT_FOUND, "Toilet of id '$id' does not exist!")

class ImageDoesNotExistException(id: String) :
    ToilappException(HttpStatus.NOT_FOUND, "Image of id '$id' does not exist!")

class UnsupportedImageContentTypeException(fileName: String) :
    ToilappException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Image '$fileName' has an unsupported type!")

class UserDoesNotExistException(id: String) :
    ToilappException(HttpStatus.NOT_FOUND, "User of id '$id' does not exist!")

class CommentDoesNotExistException(id: String) :
    ToilappException(HttpStatus.NOT_FOUND, "Comment of id '$id' does not exist!")

class RatingDoesNotExistException(id: String) :
    ToilappException(HttpStatus.NOT_FOUND, "Rating of id '$id' does not exist!")

class InvalidIdException(id: String) :
    ToilappException(HttpStatus.UNPROCESSABLE_ENTITY, "Id '$id' is not a correct hex id with 24 characters")

class InvalidRatingValueException(value: Int) :
    ToilappException(
        HttpStatus.UNPROCESSABLE_ENTITY,
        "Invalid rating value '$value'. A value must be an integer between 1 and 5"
    )
