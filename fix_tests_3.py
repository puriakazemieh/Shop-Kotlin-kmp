import re
import glob

files = glob.glob('core/config/capabilities/src/jvmTest/kotlin/com/kazemieh/config/capabilities/*.kt')
bad_ceiling = ', ceiling = CompiledFeatureCeiling(setOf("content.blog", "commerce.core", "commerce.physical", "commerce.digital", "wallet"))'
for file in files:
    with open(file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Fix FeatureManifestBootstrapCoordinatorTest
    content = content.replace('RemoteManifestResponse(503, "", null' + bad_ceiling + ')', 'RemoteManifestResponse(503, "", null)')
    content = content.replace('client(response' + bad_ceiling + ')', 'client(response)')
    
    # Fix LocalFeatureManifestSourceTest
    content = content.replace('LocalFeatureManifestSource(config)', 'LocalFeatureManifestSource(config' + bad_ceiling + ')')
    content = content.replace('LocalFeatureManifestSource(\n            config = config\n        )', 'LocalFeatureManifestSource(\n            config = config' + bad_ceiling + '\n        )')
    
    # Fix RemoteFeatureManifestClientTest
    content = content.replace('RemoteManifestResponse(304, "", null' + bad_ceiling + ')', 'RemoteManifestResponse(304, "", null)')
    content = content.replace('RemoteManifestResponse(200, body, null' + bad_ceiling + ')', 'RemoteManifestResponse(200, body, null)')
    content = content.replace('error("timeout"' + bad_ceiling + ')', 'error("timeout")')

    with open(file, 'w', encoding='utf-8') as f:
        f.write(content)
