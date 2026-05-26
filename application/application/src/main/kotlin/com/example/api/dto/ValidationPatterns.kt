package com.example.api.dto

/**
 * Centralizes reusable validation patterns for request DTOs.
 */
object ValidationPatterns {
    const val USERNAME = "^[a-z0-9._-]+$"
    const val DISPLAY_NAME = "^[\\p{L}][\\p{L}0-9 .'-]*$"
    const val PERSON_NAME = "^[\\p{L}][\\p{L} .'-]*$"
    const val LOCATION = "^$|^[\\p{L}0-9 .,'-]+$"
    const val ECOSYSTEM_NAME = "^[\\p{L}0-9][\\p{L}0-9 ._'()-]*$"
    const val TASK_TITLE = "^[\\p{L}0-9][\\p{L}0-9 .,'()+/:_-]*$"
    const val ECOSYSTEM_TYPE = "FORMICARIUM|FLORARIUM|INDOOR_PLANTS|DIY_INCUBATOR"
    const val LOG_EVENT_TYPE = "OBSERVATION|FEEDING|WATERING"
    const val TASK_TYPE = "WATERING|FEEDING|CLEANING|INSPECTION"
    const val TASK_STATUS = "OPEN|DONE|DISMISSED"
    const val DISMISSAL_REASON_OPTIONAL = "^$|TOO_SOON|NOT_RELEVANT|ALREADY_HANDLED"
}
