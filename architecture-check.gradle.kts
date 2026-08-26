tasks.register("architectureCheck") {
    group = "verification"
    description = "Checks that feature modules do not depend on other feature modules."
    
    doLast {
        val violations = mutableListOf<String>()
        val featureProjects = project.subprojects.filter { it.path.startsWith(":feature:") }
        
        for (feature in featureProjects) {
            val deps = mutableSetOf<String>()
            // Look at all configurations
            for (config in feature.configurations) {
                for (dep in config.dependencies) {
                    if (dep is ProjectDependency) {
                        deps.add(dep.dependencyProject.path)
                    }
                }
            }
            for (dep in deps) {
                if (dep.startsWith(":feature:") && dep != feature.path) {
                    violations.add("${feature.path} -> $dep")
                }
            }
        }
        
        val allowedViolations = setOf(
            ":feature:profile -> :feature:catalog",
            ":feature:profile -> :feature:academy",
            ":feature:profile -> :feature:psychtest",
            ":feature:profile -> :feature:clinic",
            
            ":feature:admin:products -> :feature:admin:options",
            ":feature:admin:products -> :feature:admin:orders",
            ":feature:admin:products -> :feature:admin:wallet",
            ":feature:admin:products -> :feature:admin:blog",
            ":feature:admin:products -> :feature:admin:academy",
            ":feature:admin:products -> :feature:admin:clinic",
            ":feature:admin:products -> :feature:admin:psychtest",
            ":feature:academy -> :feature:details",
            ":feature:clinic -> :feature:details",
            ":feature:main -> :feature:cart",
            ":feature:main -> :feature:catalog"
        )
        
        val newViolations = violations.filter { !allowedViolations.contains(it) }
        
        if (newViolations.isNotEmpty()) {
            throw GradleException("Architecture Violations found (feature depending on feature):\n" + newViolations.joinToString("\n"))
        }
        
        println("Architecture check passed! Existing violations: \n" + violations.joinToString("\n"))
    }
}


