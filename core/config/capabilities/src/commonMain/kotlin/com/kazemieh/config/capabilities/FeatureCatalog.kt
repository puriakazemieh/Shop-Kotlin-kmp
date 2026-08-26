package com.kazemieh.config.capabilities

data class FeatureDefinition(val id: String, val requires: Set<String> = emptySet())

data class ResolvedFeatures(private val values: Map<String, Boolean>) {
    fun isEnabled(id: String): Boolean = values[id] == true
    fun asMap(): Map<String, Boolean> = values
}

/** catalog ثابت v1 و resolver fail-closed وابستگی‌های FeatureManifest. */
class FeatureCatalog(private val definitions: List<FeatureDefinition> = v1Definitions) {
    private val byId = definitions.associateBy(FeatureDefinition::id)

    init {
        require(byId.size == definitions.size) { "Feature ids must be unique." }
        definitions.forEach { definition ->
            require(definition.requires.all(byId::containsKey)) { "Unknown dependency in ${definition.id}." }
        }
        definitions.forEach { visit(it.id, mutableSetOf(), mutableSetOf()) }
    }

    fun resolve(manifest: FeatureManifest): ResolvedFeatures {
        require(manifest.schemaVersion == 1) { "Unsupported manifest schema." }
        require(manifest.features.keys.all(byId::containsKey)) { "Unknown manifest feature." }
        val requested = byId.keys.associateWith { manifest.features[it] == true }
        return ResolvedFeatures(requested.mapValues { (id, enabled) -> enabled && dependenciesEnabled(id, requested) })
    }

    private fun dependenciesEnabled(id: String, requested: Map<String, Boolean>): Boolean =
        byId.getValue(id).requires.all { dependency -> requested[dependency] == true && dependenciesEnabled(dependency, requested) }

    private fun visit(id: String, visiting: MutableSet<String>, visited: MutableSet<String>) {
        if (id in visited) return
        require(visiting.add(id)) { "Feature dependency cycle at $id." }
        byId.getValue(id).requires.forEach { visit(it, visiting, visited) }
        visiting.remove(id)
        visited.add(id)
    }

    companion object {
        val v1Definitions = listOf(
            FeatureDefinition("content.blog"),
            FeatureDefinition("commerce.core"),
            FeatureDefinition("commerce.physical", setOf("commerce.core")),
            FeatureDefinition("commerce.digital", setOf("commerce.core")),
            FeatureDefinition("academy.core", setOf("content.blog")),
            FeatureDefinition("academy.quiz", setOf("academy.core")),
            FeatureDefinition("academy.certificate", setOf("academy.core")),
            FeatureDefinition("clinic.booking", setOf("content.blog")),
            FeatureDefinition("clinic.messaging", setOf("clinic.booking")),
            FeatureDefinition("psych.tests", setOf("content.blog")),
            FeatureDefinition("wallet", setOf("commerce.core")),
            FeatureDefinition("admin.mobile")
        )
    }
}
