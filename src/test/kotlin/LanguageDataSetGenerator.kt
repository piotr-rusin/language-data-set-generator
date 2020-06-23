package com.github.rtwnt.language_data_set_generator

import com.github.rtwnt.language_data.row.Code
import com.github.rtwnt.language_data.row.Value
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

    private fun prepareCodeMock(): Code {
        val code = Mockito.mock(Code::class.java)
        increment += 1
        Mockito.`when`(code.parameterId).thenReturn("parameter$increment")
        return code
    }

    private fun prepareValueMock(codeId: String?, languageId: String?, parameterId: String?): Value {
        val value = Mockito.mock(Value::class.java)
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

    @Test
    fun `generateFeatureSet generates expected set for given probabilities`() {
        val values = listOf(
                prepareValueMock("code1", "lang1", "param1"),
                prepareValueMock("code2", "lang2", "param1"),
                prepareValueMock("code3", "lang2", "param2")
        )
        val randomMock = Mockito.mock(Random::class.java)
        Mockito.`when`(randomMock.nextInt(Mockito.anyInt())).thenReturn(0)

        val generator = FeatureSetGenerator(values)

        val actual = generator.generateFeatureSet(randomMock)
        val expected = setOf("code3", "code1")

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