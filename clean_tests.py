import re
import glob

files = glob.glob('core/config/capabilities/src/jvmTest/kotlin/com/kazemieh/config/capabilities/*.kt')
for file in files:
    with open(file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Just remove ceiling from any constructor call!
    # , ceiling = CompiledFeatureCeiling(setOf("content.blog", "commerce.core", "commerce.physical", "commerce.digital", "wallet"))
    content = content.replace(', ceiling = CompiledFeatureCeiling(setOf("content.blog", "commerce.core", "commerce.physical", "commerce.digital", "wallet"))', '')
    content = content.replace('\n            , ceiling = CompiledFeatureCeiling(setOf("content.blog", "commerce.core", "commerce.physical", "commerce.digital", "wallet"))', '')
    
    with open(file, 'w', encoding='utf-8') as f:
        f.write(content)
