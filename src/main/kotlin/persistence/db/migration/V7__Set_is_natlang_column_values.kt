package com.github.rtwnt.language_data_set_generator.persistence.db.migration

import com.github.rtwnt.language_data_set_generator.persistence.*
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.jetbrains.exposed.sql.transactions.transaction

class V7__Set_is_natlang_column_values : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        transaction {
            val languageWalsIdsFromImport = readLanguageDataFromCsv().map { it.getValue(WALS_ID_KEY) }
            val allImportedLanguages = Language.find { Languages.walsId inList languageWalsIdsFromImport}
            allImportedLanguages.forEach { it.isNatlang = true }
        }
    }
}