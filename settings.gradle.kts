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

// The scaffolder, when its checkout happens to be beside this one.
//
// The dependency is written as a coordinate either way, `dev.ancaria.coderpack:
// templates`, and this line decides where that resolves from: the sibling
// build when it is there, and Maven otherwise. That is what makes a change to a
// template visible in the New Project dialog without publishing anything in
// between, and it is also why CI does not need this -- it checks out `build`
// and runs publishToMavenLocal instead, which is the path a contributor with
// one checkout takes.
val scaffolder = file("../build/gradle")
if (scaffolder.isDirectory) {
    includeBuild(scaffolder)
}
