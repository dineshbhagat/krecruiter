package com.phauer.krecruiter.applicationApi

import com.phauer.krecruiter.common.ApiPaths
import com.phauer.krecruiter.common.ApplicationState
import com.phauer.krecruiter.common.Outcome
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.time.Clock


@RestController
@RequestMapping(ApiPaths.applications)
class ApplicationController(
    private val dao: ApplicationDAO,
    private val addressValidationClient: AddressValidationClient,
    private val clock: Clock
) {
    @GetMapping
    fun getApplications(
        @RequestParam state: ApplicationState?
    ): List<ApplicationDTO> {
        val applicationEntities = dao.findAllApplications(state)
        return applicationEntities.map { it.mapToDto() }
    }

    // TODO AddressValidationService, validation, location header?
    // TODO add curl/http example for POST in README

    @PostMapping
    fun createApplication(
        @RequestBody applicationDto: ApplicationCreationDTO
    ) = when (val validationResult = addressValidationClient.validateAddress(applicationDto.street, applicationDto.city)) {
        is Outcome.Success -> {
            when (validationResult.value.valid) {
                true -> {
                    val applicationId = createApplicationWithApplicant(applicationDto)
                    ResponseEntity.created(URI("${ApiPaths.applications}/$applicationId")).build()
                }
                false -> ResponseEntity.badRequest().body("""{ "errorMessage": "Invalid address"}""")
            }
        }
        is Outcome.Error -> ResponseEntity.status(INTERNAL_SERVER_ERROR).build()
    }

    private fun createApplicationWithApplicant(applicationDto: ApplicationCreationDTO): Int {
        val now = clock.instant()
        val applicant =
            dao.createApplicant(applicationDto.firstName, applicationDto.lastName, applicationDto.street, applicationDto.city, now)
        val application = dao.createApplication(applicationDto.jobTitle, applicant.id, ApplicationState.RECEIVED, now)
        return application.id
    }
}

private fun ApplicationWithApplicantsEntity.mapToDto() = ApplicationDTO(
    id = this.id,
    fullName = "${this.firstName} ${this.lastName}",
    jobTitle = this.jobTitle,
    state = this.state,
    dateCreated = this.dateCreated
)

