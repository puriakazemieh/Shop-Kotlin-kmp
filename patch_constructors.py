import re

path = 'core/config/capabilities/src/commonMain/kotlin/com/kazemieh/config/capabilities/FeatureUseCaseGuard.kt'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('class FeatureUseCaseGuard(private val store: EffectiveFeatureStore) {', '''class FeatureUseCaseGuard(private val store: EffectiveFeatureStore) {
    @Deprecated("Use EffectiveFeatureStore", ReplaceWith("FeatureUseCaseGuard(DefaultEffectiveFeatureStore(features))"))
    constructor(features: ResolvedFeatures) : this(DefaultEffectiveFeatureStore(features))
''')

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)

path2 = 'core/navigation/src/commonMain/kotlin/com/kazemieh/navigation/FeatureRouteGuard.kt'
with open(path2, 'r', encoding='utf-8') as f:
    content2 = f.read()

content2 = content2.replace('class FeatureRouteGuard(private val store: com.kazemieh.config.capabilities.EffectiveFeatureStore) {', '''class FeatureRouteGuard(private val store: com.kazemieh.config.capabilities.EffectiveFeatureStore) {
    @Deprecated("Use EffectiveFeatureStore", ReplaceWith("FeatureRouteGuard(com.kazemieh.config.capabilities.DefaultEffectiveFeatureStore(features))"))
    constructor(features: com.kazemieh.config.capabilities.ResolvedFeatures) : this(com.kazemieh.config.capabilities.DefaultEffectiveFeatureStore(features))
''')

with open(path2, 'w', encoding='utf-8') as f:
    f.write(content2)
