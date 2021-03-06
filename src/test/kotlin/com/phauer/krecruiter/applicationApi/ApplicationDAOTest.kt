package com.phauer.krecruiter.applicationApi

import com.phauer.krecruiter.common.ApplicationState
import com.phauer.krecruiter.util.PostgreSQLInstance
import com.phauer.krecruiter.util.TestDAO
import com.phauer.krecruiter.util.createApplicantEntity
import com.phauer.krecruiter.util.createApplicationEntity
import com.phauer.krecruiter.util.toInstant
import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldContainExactly
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class ApplicationDAOTest {

    private val dao: ApplicationDAO = ApplicationDAO(PostgreSQLInstance.jdbi)
    private val testDAO = PostgreSQLInstance.jdbi.onDemand<TestDAO>()

    @BeforeEach
    fun clear() {
        testDAO.clearTables()
    }

    @Test
    fun `filtering by ApplicationState should only return the applications with the requested state`() {
        insertApplicationWithApplicant(id = 100, state = ApplicationState.REJECTED)
        insertApplicationWithApplicant(id = 200, state = ApplicationState.REJECTED)
        insertApplicationWithApplicant(id = 300, state = ApplicationState.INVITED_TO_INTERVIEW)
        insertApplicationWithApplicant(id = 400, state = ApplicationState.EMPLOYED)
        insertApplicationWithApplicant(id = 500, state = ApplicationState.RECEIVED)

        val actualApplications = dao.findAllApplications(state = ApplicationState.REJECTED)

        actualApplications.asClue {
            it.map(ApplicationWithApplicantsEntity::id).shouldContainExactly(100, 200)
        }
    }

    @Test
    fun `return all when application state is not set`() {
        insertApplicationWithApplicant(id = 100, state = ApplicationState.REJECTED)
        insertApplicationWithApplicant(id = 200, state = ApplicationState.INVITED_TO_INTERVIEW)

        val actualApplications = dao.findAllApplications(state = null)

        actualApplications.asClue {
            it.map(ApplicationWithApplicantsEntity::id).shouldContainExactly(100, 200)
        }
    }


    @Test
    fun `order by dateCreated`() {
        insertApplicationWithApplicant(id = 100, dateCreated = 100.toInstant())
        insertApplicationWithApplicant(id = 200, dateCreated = 200.toInstant())
        insertApplicationWithApplicant(id = 300, dateCreated = 3.toInstant())

        val actualApplications = dao.findAllApplications(state = null)

        actualApplications.asClue {
            it.map(ApplicationWithApplicantsEntity::id).shouldContainExactly(300, 100, 200)
        }
    }


    private fun insertApplicationWithApplicant(
        id: Int = 100,
        state: ApplicationState = ApplicationState.REJECTED,
        dateCreated: Instant = 1.toInstant()
    ) {
        testDAO.insert(createApplicantEntity(id = id, firstName = "John", lastName = "Doe"))
        testDAO.insert(createApplicationEntity(id = id, applicantId = id, state = state, dateCreated = dateCreated))
    }
}