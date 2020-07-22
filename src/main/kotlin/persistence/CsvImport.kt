package com.github.rtwnt.language_data_set_generator.persistence

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

const val NAME_KEY = "Name"
const val FAMILY_KEY = "Family"
const val AREA_KEY = "Area"
const val WALS_ID_KEY = "ID"
const val PARAMETER_ID_KEY = "Parameter_ID"
const val LANGUAGE_ID_KEY = "Language_ID"
const val CODE_ID_KEY = "Code_ID"
const val MACROAREA_KEY = "macroarea"
const val ASCII_NAME_KEY = "ascii_name"

private fun getDistinctNonBlankNames(data: Collection<Map<String, String>>, nameKey: String): Sequence<String> {
    return data.asSequence().map { it.getValue(nameKey) }
        .filter { it.isNotBlank() }
        .distinct()
}

private fun createLanguageFamilies(languageData: List<Map<String, String>>): Map<String, LanguageFamily> {
    return getDistinctNonBlankNames(languageData, FAMILY_KEY)
        .map {
            LanguageFamily.new {
                name = it
            }
        }.associateBy { it.name }
}

private fun createMacroareas(walsLanguageData: Map<String, Map<String, String>>): Map<String, Macroarea> {
    return getDistinctNonBlankNames(walsLanguageData.values, MACROAREA_KEY)
        .map {
            Macroarea.new {
                name = it
            }
        }.associateBy { it.name }
}

private fun createLanguages(
    languageData: List<Map<String, String>>,
    walsLanguageData: Map<String, Map<String, String>>
): Map<String, Language> {
    val languageFamilies = createLanguageFamilies(languageData)
    val macroareas = createMacroareas(walsLanguageData)

    return languageData.map {
        val macroareaName = walsLanguageData[it.getValue(NAME_KEY).toLowerCase()]?.getValue(MACROAREA_KEY)
        var languageMacroarea: Macroarea? = null
        if (!macroareaName.isNullOrBlank()) {
            languageMacroarea = macroareas.getValue(macroareaName)
        }

        val languageFamilyName = it.getValue(FAMILY_KEY)
        var languageFamily: LanguageFamily? = null
        if (!languageFamilyName.isBlank()) {
            languageFamily = languageFamilies.getValue(languageFamilyName)
        }
        Language.new {
            walsId = it.getValue(WALS_ID_KEY)
            name = it.getValue(NAME_KEY)
            family = languageFamily
            macroarea = languageMacroarea
        }
    }.associateBy { it.walsId }
}

private fun createtFeatureAreas(featureData: List<Map<String, String>>): Map<String, FeatureArea> {
    return getDistinctNonBlankNames(featureData, AREA_KEY)
        .map {
            FeatureArea.new {
                name = it
            }
        }.associateBy { it.name }
}

private fun createFeatures(featureData: List<Map<String, String>>): Map<String, Feature> {
    val featureAreas = createtFeatureAreas(featureData)
    return featureData.map {
        Feature.new {
            walsId = it.getValue(WALS_ID_KEY)
            name = it.getValue(NAME_KEY)
            area = featureAreas.getValue(it.getValue(AREA_KEY))
        }
    }.associateBy { it.walsId }
}

private fun createFeatureValues(
    featureValueData: List<Map<String, String>>,
    featureData: List<Map<String, String>>
): Map<String, FeatureValue> {
    val features = createFeatures(featureData)
    return featureValueData.map {
        FeatureValue.new {
            walsId = it.getValue(WALS_ID_KEY)
            name = it.getValue(NAME_KEY)
            feature = features.getValue(it.getValue(PARAMETER_ID_KEY))
        }
    }.associateBy { it.walsId }
}

private fun createLanguageFeatureValueRelations(
    languageIdToFeatureValueId: Map<String, List<String>>,
    featureValues: Map<String, FeatureValue>,
    languages: Map<String, Language>
) {
    languageIdToFeatureValueId.forEach {
        val languageWalsId = it.key
        val values = it.value.map { valueId ->
            featureValues.getValue(valueId)
        }
        languages.getValue(languageWalsId).featureValues = SizedCollection(values)
    }
}

fun getResourceFile(path: String): File {
    val p = object {}.javaClass.getResource(path)
    return File(p.path)
}

fun readDataFromCsvResource(path: String): List<Map<String, String>> {
    return csvReader().readAllWithHeader(getResourceFile(path))
}

fun importData() {
    val featureData = readDataFromCsvResource("/wals/cldf/parameters.csv")
    val featureValueData = readDataFromCsvResource("/wals/cldf/codes.csv")
    val languageData = readDataFromCsvResource("/wals/cldf/languages.csv")
    val walsLanguageData = readDataFromCsvResource("/wals/raw/walslanguage.csv")
        .associateBy { it.getValue(ASCII_NAME_KEY).toLowerCase() }
    val languageIdToFeatureValueId = readDataFromCsvResource("/wals/cldf/values.csv")
        .groupBy(
            { it.getValue(LANGUAGE_ID_KEY) },
            { it.getValue(CODE_ID_KEY) }
        )

    transaction {
        val featureValues = createFeatureValues(featureValueData, featureData)
        val languages = createLanguages(languageData, walsLanguageData)
        createLanguageFeatureValueRelations(
            languageIdToFeatureValueId,
            featureValues,
            languages
        )
    }
}