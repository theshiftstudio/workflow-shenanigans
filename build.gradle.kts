import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val versions = java.util.Properties().apply {
        java.io.FileInputStream("${rootDir.absolutePath}/versions.properties").use {
            load(it)
        }
    }

    @Suppress("UNUSED_VARIABLE")
    val composeVersion by extra(versions.getProperty("version.androidx.compose.ui"))

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
    }

    dependencies {
        classpath(Dependencies.Android.gradlePlugin)
        classpath(Dependencies.Kotlin.gradlePlugin)

        classpath(Google.dagger.hilt.android.gradlePlugin)

        classpath(Dependencies.ktlint)
        classpath(Dependencies.detekt)

        // classpath("com.vanniktech:gradle-dependency-graph-generator-plugin:_")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}

// apply(plugin = "com.vanniktech.dependency.graph.generator")

subprojects {
    repositories {
        google()
        mavenCentral()
        // jcenter()
    }

    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    afterEvaluate {
        tasks.findByName("check")
            ?.dependsOn("detekt")
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            // Treat all Kotlin warnings as errors
            allWarningsAsErrors = false

            jvmTarget = "11"

            freeCompilerArgs += listOf(
                "-progressive",
                "-Xopt-in=kotlin.RequiresOptIn",
                "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-Xopt-in=kotlin.ExperimentalStdlibApi",
            )
        }
    }

    // Configuration documentation: https://github.com/JLLeitschuh/ktlint-gradle#configuration
    configure<KtlintExtension> {
        // Prints the name of failed rules.
        verbose.set(true)
        reporters {
            // Default "plain" reporter is actually harder to read.
            reporter(ReporterType.JSON)
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}