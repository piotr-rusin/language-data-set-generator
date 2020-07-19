package com.github.rtwnt.language_data_set_generator.persistence

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*


object FeatureAreas : IntIdTable("feature_areas") {
    val name = varchar("name", 20).uniqueIndex()
}
class FeatureArea(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<FeatureArea>(FeatureAreas)
    var name by FeatureAreas.name
}


object FeatureInfluencingFeatures : Table("feature_influencing_feature_map") {
    val feature = reference("feature", Features)
    val influencingFeature = reference("influencing_feature", Features)
    override val primaryKey = PrimaryKey(feature, influencingFeature)
}
object Features : IntIdTable() {
    val walsId = varchar("wals_id", 4).uniqueIndex()
    val name = varchar("name", 104).uniqueIndex()
    val area = reference("area", FeatureAreas).index()
}
class Feature(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Feature>(Features)
    var walsId by Features.walsId
    var name by Features.name
    var area by FeatureArea referencedOn Features.area
    val influencedBy by Feature via FeatureInfluencingFeatures
    val values by FeatureValue referrersOn FeatureValues.feature
}


object FeatureValues : IntIdTable("feature_values") {
    val walsId = varchar("wals_id", 7).uniqueIndex()
    val name = varchar("name", 94)
    val feature = reference("feature", Features).index()
}
class FeatureValue(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<FeatureValue>(FeatureValues)
    var walsId by FeatureValues.walsId
    var name by FeatureValues.name
    var feature by Feature referencedOn  FeatureValues.feature
}


object LanguageFamilies : IntIdTable("language_families") {
    val name = varchar("name", 30).uniqueIndex()
}
class LanguageFamily(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<LanguageFamily>(LanguageFamilies)
    var name by LanguageFamilies.name
}


object Macroareas : IntIdTable() {
    val name = varchar("name", 13).uniqueIndex()
}
class Macroarea(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Macroarea>(Macroareas)
    var name by Macroareas.name
}


object LanguageFeatureValues : Table("language_feature_value_map") {
    val language = reference("language", Languages)
    val featureValue = reference("feature_value", FeatureValues)
    override val primaryKey = PrimaryKey(language, featureValue)
}
object Languages : IntIdTable() {
    val walsId = varchar("wals_id", 3).uniqueIndex()
    val name = varchar("name", 46).uniqueIndex()
    val family = reference("family", LanguageFamilies).index()
    val macroarea = reference("macroarea", Macroareas).index()
}
class Language(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Language>(Languages)
    var walsId by Languages.walsId
    var name by Languages.name
    var family by LanguageFamily referencedOn Languages.family
    var macroarea by Macroarea referencedOn Languages.macroarea
    var featureValues by FeatureValue via LanguageFeatureValues
}