
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