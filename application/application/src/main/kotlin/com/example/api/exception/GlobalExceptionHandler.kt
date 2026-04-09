package com.example.api.exception

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime

/**
 * Centralizes exception-to-response mapping for the REST API.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    /**
     * Converts bean validation failures into a structured 400 response.
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        exception: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> {
        val validationErrors = exception.bindingResult.fieldErrors
            .associate { fieldError -> fieldError.field to resolveValidationMessage(fieldError) }

        return buildResponse(
            status = HttpStatus.BAD_REQUEST,
            message = "Validation failed",
            path = request.requestURI,
            validationErrors = validationErrors
        )
    }

    /**
     * Converts explicit application status exceptions into API error responses.
     */
    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatusException(
        exception: ResponseStatusException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> =
        buildResponse(
            status = HttpStatus.valueOf(exception.statusCode.value()),
            message = exception.reason ?: "Request failed",
            path = request.requestURI
        )

    /**
     * Converts unexpected exceptions into a generic 500 response.
     */
    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(
        exception: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> =
        buildResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            message = "Unexpected server error",
            path = request.requestURI
        )

    /**
     * Builds the shared error response payload for all handled exceptions.
     */
    private fun buildResponse(
        status: HttpStatus,
        message: String,
        path: String,
        validationErrors: Map<String, String> = emptyMap()
    ): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(status).body(
            ApiErrorResponse(
                timestamp = OffsetDateTime.now(),
                status = status.value(),
                error = status.reasonPhrase,
                message = message,
                path = path,
                validationErrors = validationErrors
            )
        )

    /**
     * Extracts a user-friendly message for a validation error.
     */
    private fun resolveValidationMessage(fieldError: FieldError): String =
        fieldError.defaultMessage ?: "Invalid value"
}
