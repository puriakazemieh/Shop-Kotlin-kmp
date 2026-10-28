import json
import os

with open('tools/build-config/products.json', 'r') as f:
    specs = json.load(f)

out_dir = 'composeApp/build/generated/source/buildConfig/commonMain/kotlin/com/kazemieh/shop'
os.makedirs(out_dir, exist_ok=True)

with open(f'{out_dir}/GeneratedProductSpecs.kt', 'w') as f:
    f.write('package com.kazemieh.shop\n\n')
    f.write('import com.kazemieh.config.capabilities.*\n')
    f.write('import kotlin.collections.setOf\n')
    f.write('import kotlin.collections.mapOf\n\n')
    f.write('object GeneratedProductSpecs {\n')
    
    # Write specs map
    f.write('    val specs = mapOf<String, ProductBuildSpec>(\n')
    for sku, spec in specs.items():
        tenant = spec['tenant']
        branding = spec['branding']
        backend = spec['backendProfile']
        features = spec['compiledFeatureIds']
        features_str = ', '.join(f'"{x}"' for x in features)
        auth_hosts = ', '.join(f'"{x}"' for x in backend['allowedAuthHosts'])
        
        f.write(f'        "{sku}" to ProductBuildSpec(\n')
        f.write(f'            productKind = ProductKind.{spec["productKind"]},\n')
        f.write(f'            sku = "{spec["sku"]}",\n')
        f.write(f'            platform = ClientPlatform.{spec["platform"]},\n')
        f.write(f'            tenant = TenantConfig("{tenant["id"]}"),\n')
        f.write(f'            branding = BrandingConfig("{branding["id"]}", "{branding["displayName"]}", "{branding["currency"]}"),\n')
        f.write(f'            buildIdentity = BuildIdentity("com.kazemieh.shop", "1.0", 1),\n')
        f.write(f'            backendProfile = BackendProfile(BackendKind.{backend["kind"]}, "{backend["apiRoot"]}", "{backend["assetRoot"]}", setOf({auth_hosts}), {backend["contractVersion"]}, "{backend["manifestPath"]}"),\n')
        f.write(f'            compiledFeatureIds = setOf({features_str})\n')
        f.write('        ),\n')
    f.write('    )\n\n')
    
    # Write localConfigs map
    f.write('    val localConfigs = mapOf<String, LocalFeatureManifestConfig>(\n')
    for sku, spec in specs.items():
        backend = spec['backendProfile']
        tenant = spec['tenant']
        local = spec['localFeatures']
        
        contentBlog = "true" if local['contentBlog'] else "false"
        commerceCore = "true" if local['commerceCore'] else "false"
        commercePhysical = "true" if local['commercePhysical'] else "false"
        commerceDigital = "true" if local['commerceDigital'] else "false"
        
        f.write(f'        "{sku}" to LocalFeatureManifestConfig(\n')
        f.write(f'            backendKind = BackendKind.{backend["kind"]},\n')
        f.write(f'            tenantId = "{tenant["id"]}",\n')
        f.write(f'            contentBlog = {contentBlog},\n')
        f.write(f'            commerceCore = {commerceCore},\n')
        f.write(f'            commercePhysical = {commercePhysical},\n')
        f.write(f'            commerceDigital = {commerceDigital}\n')
        f.write('        ),\n')
    f.write('    )\n')
    f.write('}\n')
