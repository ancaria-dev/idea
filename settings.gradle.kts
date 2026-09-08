pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

// The one toolchain block in this project, and the reason for it is not a
// preference.
//
// Every sibling repository refuses toolchains on purpose: a build tool that
// downloads a JDK to compile a hello-world is a bad first impression, and
// `options.release` settles what comes out of whichever JDK is on the path. An
// IDE plugin is the exception. The IntelliJ Platform Gradle Plugin pins the
// compiler to the JVM the target IDE actually runs on, because a plugin
// compiled by anything else is one the IDE will not load, and it is not this
// build's decision to make. So the resolver is here to fetch that JDK on a
// machine that has not got it. CI installs Temurin 21 first and downloads
// nothing.
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "sacred-idea"
