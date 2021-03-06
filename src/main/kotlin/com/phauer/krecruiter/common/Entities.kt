package com.phauer.krecruiter.common

import org.jdbi.v3.json.Json
import java.time.Instant

data class ApplicationEntity(
    val id: Int,
    val applicantId: Int,
    val jobTitle: String,
    val state: ApplicationState,
    val dateCreated: Instant,
    @get:Json
    val attachments: String?
)

data class ApplicantEntity(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val street: String,
    val city: String,
    val dateCreated: Instant
)

enum class ApplicationState {
    RECEIVED,
    INVITED_TO_INTERVIEW,
    REJECTED,
    EMPLOYED
}
