import re

def add_ceiling(path):
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()

    mock = 'ceiling = CompiledFeatureCeiling(setOf("content.blog", "commerce.core", "commerce.physical", "commerce.digital", "wallet"))'
    
    # LocalFeatureManifestSource
    content = re.sub(r'LocalFeatureManifestSource\(([^)]*?)\)', lambda m: m.group(0) if 'ceiling' in m.group(0) else f'LocalFeatureManifestSource({m.group(1)}, {mock})' if m.group(1).strip() else f'LocalFeatureManifestSource({mock})', content)
    
    # RemoteFeatureManifestClient
    content = re.sub(r'RemoteFeatureManifestClient\(([^)]*?)\)', lambda m: m.group(0) if 'ceiling' in m.group(0) else f'RemoteFeatureManifestClient({m.group(1)}, {mock})' if m.group(1).strip() else f'RemoteFeatureManifestClient({mock})', content)
    
    # FeatureManifestBootstrapCoordinator
    content = re.sub(r'FeatureManifestBootstrapCoordinator\(([^)]*?)\)', lambda m: m.group(0) if 'ceiling' in m.group(0) else f'FeatureManifestBootstrapCoordinator({m.group(1)}, {mock})' if m.group(1).strip() else f'FeatureManifestBootstrapCoordinator({mock})', content)

    # InMemoryLastKnownGoodManifestCache
    content = re.sub(r'InMemoryLastKnownGoodManifestCache\(([^)]*?)\)', lambda m: m.group(0) if 'ceiling' in m.group(0) else f'InMemoryLastKnownGoodManifestCache({m.group(1)}{", " if m.group(1).strip() else ""}{mock})', content)

    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

test_files = [
    'core/config/capabilities/src/jvmTest/kotlin/com/kazemieh/config/capabilities/FeatureManifestBootstrapCoordinatorTest.kt',
    'core/config/capabilities/src/jvmTest/kotlin/com/kazemieh/config/capabilities/LocalFeatureManifestSourceTest.kt',
    'core/config/capabilities/src/jvmTest/kotlin/com/kazemieh/config/capabilities/RemoteFeatureManifestClientTest.kt'
]

for path in test_files:
    add_ceiling(path)
