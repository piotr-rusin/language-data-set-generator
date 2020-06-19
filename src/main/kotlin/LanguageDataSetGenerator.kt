
package com.github.rtwnt.language_data_set_generator

import com.github.rtwnt.language_data.row.Code
import com.github.rtwnt.language_data.row.Value
import kotlin.random.Random

fun getFeatureValueLanguageSetMap(values: List<Value>, codes:  Map<String, Code>): Map<Code, Set<String>> {
    val codeLanguageList: MutableMap<Code, MutableSet<String>> = mutableMapOf()
    for (v in values) {
        val code = codes[v.codeId] ?: error("Could not find a code for id ${v.codeId}")
        val languages = codeLanguageList.getOrDefault(code, mutableSetOf())
        languages.add(v.languageId)
        codeLanguageList[code] = languages
    }

    return codeLanguageList
}

fun getProbabilitiesOfOccurenceOfDependentFeatures(languagesByCode: Map<Code, Set<String>>): Map<Code, Map<Code, Double>> {
    val probabilitiesOfCooccurrence = mutableMapOf<Code, Map<Code, Double>>()

    for (first in languagesByCode.keys) {
        val probabilities = mutableMapOf<Code, Double>()
        probabilitiesOfCooccurrence[first] = probabilities
        for (second in languagesByCode.keys) {
            val langsWithFirstFeatureValue = languagesByCode.getOrDefault(first, setOf())
            val langsWithSecondFeaturValue = languagesByCode.getOrDefault(second, setOf())
            val langsWithBoth = langsWithFirstFeatureValue intersect langsWithSecondFeaturValue

            val divisor = langsWithFirstFeatureValue.size
            var probability = 0.0
            if (divisor != 0) {
                probability = langsWithBoth.size.toDouble().div(divisor)
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
    val coveredParameters = mutableSetOf<String>()
    while (codePool.isNotEmpty()) {
        val newCode = codePool.random(random)
        codePool.remove(newCode)
        
        if ((newCode.parameterId in coveredParameters) or (selectedFeatures.any { probabilities[it]?.get(newCode) ?: 0.0 < minimalProbability })) {
            continue
        }

        selectedFeatures.add(newCode)
        coveredParameters.add(newCode.parameterId)
    }

    return selectedFeatures
}