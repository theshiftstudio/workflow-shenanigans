object Versions {
    const val minSdk = 24
    const val targetSdk = 31
    const val buildToolsVersion = "31.0.0"
}

object Dependencies {

    object Android {

        const val gradlePlugin = "com.android.tools.build:gradle:_"

        const val coreLibraryDesugaring = "com.android.tools:desugar_jdk_libs:_"
    }

    object AndroidX {

        object Compose {

            const val uiToolingPreview = "androidx.compose.ui:ui-tooling-preview:_"
        }

        object Hilt {

            const val navCompose = "androidx.hilt:hilt-navigation-compose:_"
        }
    }

    const val detekt = "io.gitlab.arturbosch.detekt:detekt-gradle-plugin:_"

    object Kotlin {

        const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:_"
    }

    const val ktlint = "org.jlleitschuh.gradle:ktlint-gradle:_"

    object Square {

        object Workflow {
            const val coreJvm = "com.squareup.workflow1:workflow-core-jvm:_"
            const val compose = "com.squareup.workflow1:workflow-ui-compose:_"
            const val composeTooling = "com.squareup.workflow1:workflow-ui-compose-tooling:_"

            const val uiCoreAndroid = "com.squareup.workflow1:workflow-ui-core-android:_"
        }
    }
}
