
package com.github.rtwnt.language_data_set_generator

import com.github.rtwnt.language_data.row.Value
import java.util.*
import kotlin.random.Random

fun getCodeIdToLanguageIdMap(values: List<Value>): Map<String, Set<String>> {
    return values.groupBy { it.codeId }
            .entries
            .associate { it.key to it.value.map { value -> value.languageId }.toSet() }
}

fun getProbabilitiesOfOccurenceOfDependentFeatures(languagesByCode: Map<String, Set<String>>): Map<String, Map<String, Double>> {
    val probabilitiesOfCooccurrence = mutableMapOf<String, Map<String, Double>>()

    for (first in languagesByCode.keys) {
        val probabilities = mutableMapOf<String, Double>()
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

class FeatureSetGenerator(values: List<Value>) {
    private var parameterIdsByCount: List<String>
    private var parameterIdToCodeIds: Map<String, List<String>>
    private var probabilities: Map<String, Map<String, Double>>
    private var codeIdToLanguageId: Map<String, Set<String>>

    init {
        values.forEach {
            if (it.codeId.isNullOrBlank()) {
                error("Code id can't be null or empty")
            }

            if (it.parameterId.isNullOrBlank()) {
                error("Parameter id can't be null or empty")
            }

            if (it.languageId.isNullOrBlank()) {
                error("Language id can't be null or empty")
            }
        }
        this.codeIdToLanguageId = getCodeIdToLanguageIdMap(values)
        this.probabilities = getProbabilitiesOfOccurenceOfDependentFeatures(this.codeIdToLanguageId)
        this.parameterIdToCodeIds = values.groupBy { it.parameterId }
                .entries
                .associate { it.key to it.value.map { value -> value.codeId } }
        this.parameterIdsByCount = values.groupBy { it.parameterId }
                .entries
                .map { it.key to it.value.map { value -> value.languageId }.size }
                .sortedBy { it.second }
                .map { it.first }
    }

    fun generateFeatureSet(random: Random): Map<String, String> {
        val parameterIdPool = LinkedList(this.parameterIdsByCount)
        val paramIdToSelectedValueId = mutableMapOf<String, String>()
        val parameterIdToValueIdListForRandomChoice = mutableMapOf<String, List<String>>()
        while(parameterIdPool.isNotEmpty()) {
            val nextParamId = parameterIdPool.pop()
            // probably impossible to happen due to all parameter and parameter value ids ultimately coming from data provided in values
            val nextParamValueIds = parameterIdToCodeIds[nextParamId] ?: error("Couldn't find value ids for parameter id $nextParamId")
            val availableValues = nextParamValueIds.filter { codeId ->
                paramIdToSelectedValueId.any {
                    // same as above
                    val prob = probabilities[it.value]?.get(codeId) ?: error("Couldn't find probability for $codeId and ${it.value}")
                    prob in 0.0..1.0
                }
            }
            var nextValue: String? = null
            if (availableValues.isNotEmpty()) {
                nextValue = availableValues.random(random)
            }
            if (nextValue == null) {
                parameterIdToValueIdListForRandomChoice[nextParamId] = nextParamValueIds
            } else {
                paramIdToSelectedValueId[nextParamId] = nextValue
            }
        }
        parameterIdToValueIdListForRandomChoice.forEach { paramIdToSelectedValueId[it.key] = it.value.random(random) }

        return paramIdToSelectedValueId
    }
}