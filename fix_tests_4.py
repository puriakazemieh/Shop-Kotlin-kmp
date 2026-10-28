import re

path = 'core/config/capabilities/src/jvmTest/kotlin/com/kazemieh/config/capabilities/LocalFeatureManifestSourceTest.kt'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

bad = ', ceiling = CompiledFeatureCeiling(setOf("content.blog", "commerce.core", "commerce.physical", "commerce.digital", "wallet"))'

content = content.replace(bad, '')
content = content.replace('.resolveFor(BackendKind.WORDPRESS)', bad + ').resolveFor(BackendKind.WORDPRESS)')
content = content.replace(')\n\n        assertFalse(invalid.resolveFor(BackendKind.WORDPRESS)', bad + ')\n\n        assertFalse(invalid.resolveFor(BackendKind.WORDPRESS)')

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
