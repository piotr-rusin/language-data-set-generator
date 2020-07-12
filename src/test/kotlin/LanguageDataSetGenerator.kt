package com.github.rtwnt.language_data_set_generator

import com.github.rtwnt.language_data.row.Code as CodeRow
import com.github.rtwnt.language_data.row.Value as ValueRow
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import java.util.stream.Stream
import kotlin.random.Random

class LanguageDataSetGeneratorTests {

    private var increment = 0

    private fun prepareCodeMock(): CodeRow {
        val code = Mockito.mock(CodeRow::class.java)
        increment += 1
        Mockito.`when`(code.parameterId).thenReturn("parameter$increment")
        return code
    }

    private fun prepareValueMock(codeId: String?, languageId: String?, parameterId: String?): ValueRow {
        val value = Mockito.mock(ValueRow::class.java)
        Mockito.`when`(value.codeId).thenReturn(codeId)
        Mockito.`when`(value.languageId).thenReturn(languageId)
        Mockito.`when`(value.parameterId).thenReturn(parameterId)
        return value
    }

    @Test
    fun `getCodeIdToLanguageIdMap returns expected map`() {
        val values = listOf(
            prepareValueMock("code1", "lang1", "param1"),
            prepareValueMock("code1", "lang2", "param1"),
            prepareValueMock("code2", "lang3", "param1")
        )

        val expected = mapOf(
            "code1" to setOf("lang1", "lang2"),
            "code2" to setOf("lang3")
        )
        val actual = getCodeIdToLanguageIdMap(values)

        Assertions.assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `getProbabilitiesOfOccurenceOfDependentFeatures returns expected map`() {
        val languagesByCode = mapOf(
            "code1" to setOf("lang1", "lang2", "lang3"),
            "code2" to setOf("lang1", "lang2", "lang3"),
            "code3" to setOf("lang4", "lang2")
        )

        val expected = mapOf(
            "code1" to mapOf("code1" to 1.0, "code2" to 1.0, "code3" to 1.0 / 3.0),
            "code2" to mapOf("code1" to 1.0, "code2" to 1.0, "code3" to 1.0 / 3.0),
            "code3" to mapOf("code1" to 0.5, "code2" to 0.5, "code3" to 1.0)
        )
        val actual = getProbabilitiesOfOccurenceOfDependentFeatures(languagesByCode)

        Assertions.assertThat(actual).isEqualTo(expected)
    }

    @ParameterizedTest
    @MethodSource("provideInvalidConfig")
    fun `generateFeatureSet throws IllegalStateException for given config`(config: FeatureSetGenerationConfig) {
        val values = listOf(
                prepareValueMock("code1", "lang1", "param1"),
                prepareValueMock("code2", "lang2", "param1"),
                prepareValueMock("code3", "lang2", "param2")
        )
        val randomMock = Mockito.mock(Random::class.java)
        Mockito.`when`(randomMock.nextInt(Mockito.anyInt())).thenReturn(0)

        val generator = FeatureSetGenerator(values)
        Assertions.assertThatThrownBy { generator.generateFeatureSet(config, randomMock) }.isInstanceOf(IllegalStateException::class.java)
    }

    @ParameterizedTest
    @MethodSource("provideValidConfigAndExpectedResult")
    fun `generateFeatureSet generates expected set for given probabilities`(config: FeatureSetGenerationConfig, expected: Set<String>) {
        val values = listOf(
                prepareValueMock("code1", "lang1", "param1"),
                prepareValueMock("code2", "lang2", "param1"),
                prepareValueMock("code3", "lang2", "param2"),
                prepareValueMock("code3", "lang1", "param2")
        )
        val randomMock = Mockito.mock(Random::class.java)
        Mockito.`when`(randomMock.nextInt(Mockito.anyInt())).thenReturn(0)

        val generator = FeatureSetGenerator(values)

        val actual = generator.generateFeatureSet(config, randomMock)

        Assertions.assertThat(actual).isEqualTo(expected)
    }

    companion object {
        @JvmStatic
        private fun provideCodeLangAndParamIdsFor_generateFeatureSet_nullOrBlankTest(): Stream<Arguments> {
            return Stream.of(
                    Arguments.of(null, "lang1", "param1"),
                    Arguments.of("", "lang1", "param1"),
                    Arguments.of("  ", "lang1", "param1"),
                    Arguments.of("code1", null, "param1"),
                    Arguments.of("code1", "", "param1"),
                    Arguments.of("code1", "  ", "param1"),
                    Arguments.of("code1", "lang1", null),
                    Arguments.of("code1", "lang1", ""),
                    Arguments.of("code1", "lang1", "   ")
            )
        }

        @JvmStatic
        private fun provideInvalidConfig(): Stream<Arguments> {
            return Stream.of(
                    Arguments.of(FeatureSetGenerationConfig(setOf("unknown"), setOf(), 0.0, 1.0)),
                    Arguments.of(FeatureSetGenerationConfig(setOf("code1", "code2"), setOf(), 0.0, 1.0)),
                    Arguments.of(FeatureSetGenerationConfig(setOf(), setOf("code3"), 0.0, 1.0)),
                    Arguments.of(FeatureSetGenerationConfig(setOf("code1"), setOf("code1"), 0.0, 1.0)),
                    Arguments.of(FeatureSetGenerationConfig(setOf(), setOf("unknown"), 0.0, 1.0)),
                    Arguments.of(FeatureSetGenerationConfig(setOf(), setOf(), -0.1, 1.0)),
                    Arguments.of(FeatureSetGenerationConfig(setOf(), setOf(), 0.0, 1.1)),
                    Arguments.of(FeatureSetGenerationConfig(setOf(), setOf(), 0.7, 0.5))
            )
        }

        @JvmStatic
        private fun provideValidConfigAndExpectedResult(): Stream<Arguments> {
            return Stream.of(
                    Arguments.of(
                            FeatureSetGenerationConfig(setOf(), setOf(), 0.0, 1.0),
                            setOf("code3", "code1")
                    ),
                    Arguments.of(
                            FeatureSetGenerationConfig(setOf("code2"), setOf(), 0.0, 1.0),
                            setOf("code2", "code3")
                    ),
                    Arguments.of(
                            FeatureSetGenerationConfig(setOf(), setOf("code1"), 0.0, 1.0),
                            setOf("code2", "code3")
                    )
            )
        }
    }


    @ParameterizedTest
    @MethodSource("provideCodeLangAndParamIdsFor_generateFeatureSet_nullOrBlankTest")
    fun `FeatureSetGenerator constructor throws IllegalStateException for blank or null code, language or parameter ids in a Value instance`(
        codeId: String?, languageId: String?, parameterId: String?) {
        val values = listOf(
            prepareValueMock(codeId, languageId, parameterId)
        )

        Assertions.assertThatThrownBy { FeatureSetGenerator(values) }.isInstanceOf(IllegalStateException::class.java)
    }
}

class DataModelTests {
    @Test
    fun `Feature readAllFromFile correctly reads data`() {
        val features = Feature.readAllFromFile("src/main/resources/wals/cldf/parameters.csv")
        val actual = features["1A"]
        val expected = Feature("1A", "Consonant Inventories", "Phonology")
        Assertions.assertThat(actual).isEqualTo(expected)
    }

    private fun featureValueReadFromFiles(): Map<String, FeatureValue> {
        return FeatureValue.readFromFiles(
                "src/main/resources/wals/cldf/codes.csv",
                "src/main/resources/wals/cldf/parameters.csv"
        )
    }

    @Test
    fun `FeatureValue readFromFiles correctly reads data`() {
        val values = featureValueReadFromFiles()
        val actual = values["1A-2"]
        val expected = FeatureValue("1A-2", "Moderately small", Feature("1A", "Consonant Inventories", "Phonology"))
        Assertions.assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Language readFromFiles correctly reads data`() {
        val featureValues = featureValueReadFromFiles()
        val languages = Language.readFromFiles(
                "src/main/resources/wals/cldf/languages.csv",
                "src/main/resources/wals/raw/walslanguage.csv",
                "src/main/resources/wals/cldf/values.csv",
                featureValues
        )
        val actual = languages["amm"]
        val expected = Language(
                "amm",
                "Ama",
                "Left May",
                "Papunesia",
                featureValues.filter { it.key in setOf("86A-1", "87A-3", "97A-5") }.values.toList()
        )
        Assertions.assertThat(actual).isEqualTo(expected)
    }
}