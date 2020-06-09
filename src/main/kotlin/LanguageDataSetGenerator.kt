
package com.github.piotr_rusin.language_data_set_generator

import com.github.piotr_rusin.language_data.code.Code
import com.github.piotr_rusin.language_data.language.Language
import com.github.piotr_rusin.language_data.value.Value


fun getFeatureValueLanguageSetMap(values: List<Value>): Map<Code, Set<Language>> {
    val codeLanguageList: MutableMap<Code, MutableSet<Language>> = mutableMapOf()
    for (v in values) {
        val languages = codeLanguageList.getOrDefault(v.code, mutableSetOf())
        languages.add(v.language)
        codeLanguageList[v.code] = languages
    }

    return codeLanguageList
}

fun getProbabilitiesOfOccurenceOfDependentFeatures(languagesByCode: Map<Code, Set<Language>>): Map<Code, Map<Code, Double>> {
    val probabilitiesOfCooccurrence = mutableMapOf<Code, Map<Code, Double>>()

    for (first in languagesByCode.keys) {
        val probabilities = mutableMapOf<Code, Double>()
        probabilitiesOfCooccurrence[first] = probabilities
        for (second in languagesByCode.keys) {
            val firstLanguages = languagesByCode.getOrDefault(first, setOf())
            val secondLanguages = languagesByCode.getOrDefault(second, setOf())
            val commonLanguages = firstLanguages intersect secondLanguages

            val divisor = firstLanguages.size
            var probability = 0.0
            if (divisor != 0) {
                probability = commonLanguages.size.toDouble().div(divisor)
            }
            probabilities[second] = probability
        }
    }

    return probabilitiesOfCooccurrence
}