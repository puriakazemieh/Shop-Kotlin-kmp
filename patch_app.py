import re

path = 'composeApp/src/commonMain/kotlin/com/kazemieh/shop/App.kt'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

import_stmt = 'import com.kazemieh.config.capabilities.EffectiveFeatureStore\nimport com.kazemieh.config.capabilities.DefaultEffectiveFeatureStore\n'
content = content.replace('import com.kazemieh.config.capabilities.CompiledFeatureCeiling', import_stmt + 'import com.kazemieh.config.capabilities.CompiledFeatureCeiling')

old_block = '''                single {
                    val localConfig = GeneratedProductSpecs.localConfigs[sku] ?: throw IllegalStateException("Unknown SKU local config: $sku")
                    val source = LocalFeatureManifestSource(localConfig, ceiling = get())
                    source.resolveFor(get<ProductBuildSpec>().backendProfile.kind)
                }'''
new_block = old_block + '\n                single<EffectiveFeatureStore> { DefaultEffectiveFeatureStore(get()) }'

content = content.replace(old_block, new_block)

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
