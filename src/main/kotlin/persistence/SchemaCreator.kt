package com.github.rtwnt.language_data_set_generator.persistence

import com.github.rtwnt.language_data_set_generator.DatabaseConfig
import com.typesafe.config.ConfigFactory
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * A convenience tool for generating database schema based on Exposed table classes.
 * Intented to be execuded only during development and when database is empty.
 * The resulting schema will be used to generate table creation script that will be
 * included as a migration script to be executed by FlywayDB during deployment
 * or maintenence.
 *
 */
fun main() {
    ConfigFactory.defaultApplication()
    DatabaseConfig().initConnection()

    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(FeatureAreas, FeatureInfluencingFeatures, Features,
            FeatureValues, LanguageFamilies, Macroareas, LanguageFeatureValues, Languages)
    }
}