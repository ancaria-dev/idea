import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.intellij)
}

group = property("group") as String
version = property("pluginVersion") as String

repositories {
    // The scaffolder is a Maven Central artifact. mavenLocal() stays first so
    // `publishToMavenLocal` in a checkout of ancaria-dev/build can override it
    // to test an unreleased template change.
    mavenLocal()
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity(providers.gradleProperty("platformVersion"))

        // Java for the PSI the gutter icons read, Gradle for the project the
        // wizard links after it has written one. Kotlin and Groovy are not
        // here: the markers go through UAST, which is platform API, and the
        // registrations for those two languages sit in optional config files
        // that do nothing when the plugin providing the language is absent.
        bundledPlugins("com.intellij.java", "com.intellij.gradle")

        pluginVerifier()
        zipSigner()
        // No testFramework() here. The tests in this repository start no IDE
        // and need no fixture -- they are the catalogue, the game folder lookup
        // and the call into the scaffolder -- and the platform test framework
        // registers a JUnit session listener that has to be able to start one.
        // Adding it back means adding the fixture dependencies with it.
    }

    // The whole of `coderpack new`, minus the argument parsing: the templates,
    // the languages, the build DSLs and the Gradle wrapper, inside one jar.
    // Kotlin comes off it because the IDE hands this plugin a standard library
    // already, and two of those on one classpath is a NoClassDefFoundError with
    // a confusing name on it.
    implementation(libs.templates) {
        exclude(group = "org.jetbrains.kotlin")
    }

    testImplementation(kotlin("test"))
    testRuntimeOnly(libs.junit.launcher)
    // The tests are JUnit 5. This is JUnit 4, and it is here because the IDE
    // distribution sits on the test classpath and carries service
    // registrations that reference it: without it the test executor cannot
    // start, and the failure names org/junit/runners/model/Statement rather
    // than anything in this repository.
    testRuntimeOnly(libs.junit4)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21

        // Compiled by the current Kotlin, against the language and the standard
        // library the oldest supported IDE carries. The compiler version is
        // this build's business; what a 2025.2 has to be able to load is not,
        // and an api version pinned here is what stops a call to a stdlib
        // function that shipped later from compiling quietly and failing on
        // somebody's machine.
        languageVersion = KotlinVersion.KOTLIN_2_2
        apiVersion = KotlinVersion.KOTLIN_2_2
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
    options.encoding = "UTF-8"
}

intellijPlatform {
    // Everything a reader of the plugin page sees is in plugin.xml, which is
    // where a plugin's description belongs. Only the three numbers that come
    // out of gradle.properties are patched in, because CI reads the same lines
    // to decide whether this build is a release.
    pluginConfiguration {
        version = providers.gradleProperty("pluginVersion")
        ideaVersion {
            sinceBuild = providers.gradleProperty("sinceBuild")
            untilBuild = providers.gradleProperty("untilBuild")
        }
    }

    // Marketplace refuses an unsigned upload from an account that has ever
    // uploaded a signed one, so this is not optional past the first release.
    // The three values are secrets in CI and absent everywhere else, which
    // leaves the task disabled rather than failing a local build.
    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }

    pluginVerification {
        ides {
            recommended()
        }
    }

    // This starts a headless IDE to index the settings page for the search
    // box. It is the slowest task in the build by a wide margin and it changes
    // nothing about whether the plugin works, so it is off and the Settings
    // page is found the way every other page is: under Tools.
    buildSearchableOptions = false
}

tasks.test {
    useJUnitPlatform()
}
