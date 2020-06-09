package com.github.piotr_rusin.language_data_set_generator

import com.github.piotr_rusin.language_data.code.Code
import com.github.piotr_rusin.language_data.language.Language
import com.github.piotr_rusin.language_data.parameter.Parameter
import com.github.piotr_rusin.language_data.value.Value
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import kotlin.random.Random

class LanguageDataSetGeneratorTests {
    private fun prepareCodeMock(): Code {
        val code = Mockito.mock(Code::class.java)
        Mockito.`when`(code.parameter).thenReturn(Mockito.mock(Parameter::class.java))
        return code
    }

    private fun prepareLanguageMock(): Language {
        return Mockito.mock(Language::class.java)
    }

    private fun prepareValueMock(code: Code, language: Language): Value {
        val value = Mockito.mock(Value::class.java)
        Mockito.`when`(value.code).thenReturn(code)
        Mockito.`when`(value.language).thenReturn(language)
        return value
    }

    @Test
    fun `getFeatureValueLanguageSetMap returns expected map`() {
        val languages = (0..2).map { prepareLanguageMock() }
        val codes = (0..1).map { prepareCodeMock() }
        val values = listOf(
            prepareValueMock(codes[0], languages[0]),
            prepareValueMock(codes[0], languages[1]),
            prepareValueMock(codes[1], languages[2])
        )

        val expected = mapOf(
            codes[0] to setOf(languages[0], languages[1]),
            codes[1] to setOf(languages[2])
        )
        val actual = getFeatureValueLanguageSetMap(values)

        Assertions.assertThat(actual).isEqualTo(expected)
    }
}