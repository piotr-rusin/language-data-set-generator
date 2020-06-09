
package com.github.piotr_rusin.language_data_set_generator

import com.github.piotr_rusin.language_data.code.Code
import com.github.piotr_rusin.language_data.language.Language
import com.github.piotr_rusin.language_data.parameter.Parameter
import com.github.piotr_rusin.language_data.value.Value
import kotlin.random.Random

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

fun generateFeatureSet(probabilities: Map<Code, Map<Code, Double>>, random: Random): Set<Code> {
    val minimalProbability = 0.1

    val codePool = probabilities.keys.toMutableSet()
    val selectedFeatures = mutableSetOf<Code>()
    val coveredParameters = mutableSetOf<Parameter>()
    while (codePool.isNotEmpty()) {
        val newCode = codePool.random(random)
        codePool.remove(newCode)

        if ((newCode.parameter in coveredParameters) or (selectedFeatures.any { probabilities[it]?.get(newCode) ?: 0.0 < minimalProbability })) {
            continue
        }

        selectedFeatures.add(newCode)
        coveredParameters.add(newCode.parameter)
    }

    return selectedFeatures
}