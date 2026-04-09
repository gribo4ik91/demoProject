package com.example.api.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Wraps paginated API results together with pagination metadata.
 */
@Schema(description = "Generic paged API response wrapper.")
data class PagedResponse<T>(
    @field:Schema(description = "Current page index, zero-based.", example = "0")
    val page: Int,

    @field:Schema(description = "Requested page size.", example = "5")
    val size: Int,

    @field:Schema(description = "Total number of matching items.", example = "12")
    val totalElements: Long,

    @field:Schema(description = "Total number of pages available.", example = "3")
    val totalPages: Int,

    @field:Schema(description = "Whether there is another page after the current one.", example = "true")
    val hasNext: Boolean,

    @field:Schema(description = "Whether there is a previous page before the current one.", example = "false")
    val hasPrevious: Boolean,

    val items: List<T>
)
