filepath = r"D:\Android\AndroidStudioProjects\kmp-shop\composeApp\build.gradle.kts"
with open(filepath, "r", encoding="utf-8") as f:
    lines = f.readlines()

new_lines = []
skip = False
braces = 0

for line in lines:
    if line.strip().startswith("android {"):
        skip = True
        braces = 1
        continue
        
    if line.strip().startswith("afterEvaluate {"):
        skip = True
        braces = 1
        continue
        
    if line.strip().startswith("debugImplementation(libs.compose.uiTooling)"):
        continue

    if skip:
        if "{" in line:
            braces += line.count("{")
        if "}" in line:
            braces -= line.count("}")
        if braces <= 0:
            skip = False
        continue
        
    # We also need to add androidLibrary { namespace = "com.kazemieh.shop" } to kotlin block
    # But wait, it's easier to just add it after kotlin {
    if line.strip() == "kotlin {":
        new_lines.append(line)
        new_lines.append('    androidLibrary {\n')
        new_lines.append('        namespace = "com.kazemieh.shop"\n')
        new_lines.append('    }\n')
        continue
        
    new_lines.append(line)

with open(filepath, "w", encoding="utf-8") as f:
    f.writelines(new_lines)
