import os
import glob
import re

test_files = glob.glob('core/config/capabilities/src/jvmTest/kotlin/com/kazemieh/config/capabilities/*.kt')

mock_ceiling = 'CompiledFeatureCeiling(setOf("content.blog", "commerce.core", "commerce.physical", "commerce.digital", "wallet"))'

for path in test_files:
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()

    # CompiledFeaturePolicyTest
    content = content.replace('CompiledFeatureCeiling.shopOnly', mock_ceiling)
    
    # Coordinator
    content = content.replace('InMemoryLastKnownGoodManifestCache()', f'InMemoryLastKnownGoodManifestCache(ceiling = {mock_ceiling})')
    content = content.replace('client(transport)', f'RemoteFeatureManifestClient(profile, "tenant-1", transport, ceiling = {mock_ceiling})')
    
    # LastKnownGoodManifestCacheTest
    content = content.replace('val cache = InMemoryLastKnownGoodManifestCache()', f'val cache = InMemoryLastKnownGoodManifestCache(ceiling = {mock_ceiling})')
    
    # LocalFeatureManifestSourceTest
    content = content.replace('val source = LocalFeatureManifestSource(config)', f'val source = LocalFeatureManifestSource(config, ceiling = {mock_ceiling})')
    content = content.replace('val custom = LocalFeatureManifestSource(\n            config = config\n        )', f'val custom = LocalFeatureManifestSource(config = config, ceiling = {mock_ceiling})')
    
    # RemoteFeatureManifestClientTest
    content = content.replace('timeoutMillis = 1_250L\n        )', f'timeoutMillis = 1_250L,\n            ceiling = {mock_ceiling}\n        )')
    content = content.replace('transport = RemoteManifestTransport { RemoteManifestResponse(304, "", null) }', f'transport = RemoteManifestTransport {{ RemoteManifestResponse(304, "", null) }},\n            ceiling = {mock_ceiling}')
    content = content.replace('transport = RemoteManifestTransport { RemoteManifestResponse(200, body, null) }', f'transport = RemoteManifestTransport {{ RemoteManifestResponse(200, body, null) }},\n                ceiling = {mock_ceiling}')
    content = content.replace('transport = RemoteManifestTransport { error("timeout") }', f'transport = RemoteManifestTransport {{ error("timeout") }},\n            ceiling = {mock_ceiling}')
    
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)
