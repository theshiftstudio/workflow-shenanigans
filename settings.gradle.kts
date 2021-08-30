plugins {
    id("com.gradle.enterprise") version "3.1.1"
    id("de.fayard.refreshVersions") version "0.20.0"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}

rootProject.name = "Workflow Shenanigans"

include(
    ":app"
)
