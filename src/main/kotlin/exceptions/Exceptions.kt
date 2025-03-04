package com.northshore.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Exception thrown when a requested resource is not found
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
class ResourceNotFoundException(message: String) : RuntimeException(message)

/**
 * Exception thrown when the provided data is invalid
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidDataException(message: String) : RuntimeException(message)