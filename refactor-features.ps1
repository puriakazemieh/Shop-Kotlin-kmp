Get-ChildItem feature/ -Recurse -Filter build.gradle.kts | ForEach-Object {
    $c = Get-Content $_.FullName -Raw
    $c = $c -replace "(?s)plugins \{.*?\}", "plugins {`r`n    id(`"carmilla.compose`")`r`n}"
    $c = $c -replace "(?s)    val xcfName = .*?jvm\(\)", ""
    $c = $c -replace "compileSdk = 36\r?\n\s+minSdk = 24", ""
    $c = $c -replace "(?s)\r?\n    js \{\r?\n        browser\(\)\r?\n    \}", ""
    $c = $c -replace "(?s)\r?\n    jvm\(\)", ""
    Set-Content $_.FullName -Value $c
}
