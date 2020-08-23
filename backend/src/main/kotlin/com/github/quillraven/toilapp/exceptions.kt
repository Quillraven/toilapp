package com.github.quillraven.toilapp

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
                this.replace("status", error.statusCode.value())
                this.replace("error", error.statusCode.reasonPhrase)
                this.replace("message", error.statusText)
            }
        }
    }
}

abstract class ToilappException(statusCode: HttpStatus, statusText: String) :
    HttpStatusCodeException(statusCode, statusText)

class ToiletDoesNotExistException(id: String) :
    ToilappException(HttpStatus.NOT_FOUND, "Toilet of id |$id| does not exist!")
