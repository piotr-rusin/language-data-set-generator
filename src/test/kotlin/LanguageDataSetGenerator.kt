package com.github.rtwnt.language_data_set_generator

import com.github.rtwnt.language_data.row.Code
import com.github.rtwnt.language_data.row.Value
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import kotlin.random.Random

class LanguageDataSetGeneratorTests {

    private var increment = 0

    private fun prepareCodeMock(): Code {
        val code = Mockito.mock(Code::class.java)
        increment += 1
        Mockito.`when`(code.parameterId).thenReturn("parameter$increment")
        return code
    }

    private fun prepareValueMock(codeId: String, languageId: String, parameterId: String): Value {
        val value = Mockito.mock(Value::class.java)
        Mockito.`when`(value.codeId).thenReturn(codeId)
        Mockito.`when`(value.languageId).thenReturn(languageId)
        Mockito.`when`(value.parameterId).thenReturn(parameterId)
        return value
    }

    @Test
    fun `getFeatureValueLanguageSetMap returns expected map`() {
        val values = listOf(
            prepareValueMock("code1", "lang1", "param1"),
            prepareValueMock("code1", "lang2", "param1"),
            prepareValueMock("code2", "lang3", "param1")
        )

        val expected = mapOf(
            "code1" to setOf("lang1", "lang2"),
            "code2" to setOf("lang3")
        )
        val actual = getFeatureValueLanguageSetMap(values)

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
        val codes = (0..3).map { prepareCodeMock() }
        Mockito.`when`(codes[0].parameterId).thenReturn("param1")
        Mockito.`when`(codes[1].parameterId).thenReturn("param1")
        val probabilities = mapOf(
                codes[0] to mapOf(codes[0] to 1.0, codes[1] to 0.0, codes[2] to 0.0, codes[3] to 0.4),
                codes[1] to mapOf(codes[0] to 0.0, codes[1] to 1.0, codes[2] to 0.2, codes[3] to 0.5),
                codes[2] to mapOf(codes[0] to 0.6, codes[1] to 1.0, codes[2] to 1.0, codes[3] to 0.1),
                codes[3] to mapOf(codes[0] to 0.6, codes[1] to 1.0, codes[2] to 1.0, codes[3] to 1.0)
        )
        val randomMock = Mockito.mock(Random::class.java)
        Mockito.`when`(randomMock.nextInt(Mockito.anyInt())).thenReturn(0)

        val actual = generateFeatureSet(probabilities, randomMock)
        val expected = setOf(codes[0], codes[3])

        Assertions.assertThat(actual).isEqualTo(expected)
    }
}