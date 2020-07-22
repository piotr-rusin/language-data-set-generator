package com.github.rtwnt.language_data_set_generator.persistence

import com.github.rtwnt.language_data_set_generator.DatabaseConfig
import com.typesafe.config.ConfigFactory
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CsvImportTest {

    @BeforeEach
    fun setup() {
        val testConfig = ConfigFactory.parseMap(mutableMapOf(
            "database.driver" to "org.postgresql.Driver",
            "database.user" to "langgen-user",
            "database.password" to "",
            "database.url" to "jdbc:postgresql:langgen-test"
        ))
        val conf = DatabaseConfig(testConfig)
        conf.initConnection()
        conf.executeMigrationScripts()
    }

    @Test
    fun testImportData() {
        Assertions.assertDoesNotThrow { importData() }
    }

    @AfterEach
    fun tearDown() {
        transaction {
            val conn = TransactionManager.current().connection
            val statement = conn.prepareStatement("drop table feature_areas, feature_influencing_feature_map, feature_values, features, flyway_schema_history, language_families, language_feature_value_map, languages, macroareas cascade;", false)
            statement.executeUpdate()
        }
    }

}