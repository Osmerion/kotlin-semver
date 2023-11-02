import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn

plugins {
    alias(libs.plugins.binary.compatibility.validator)
    alias(libs.plugins.dokkatoo.html)
    alias(libs.plugins.gradle.toolchain.switches)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.plugin.serialization)
    id("com.osmerion.maven-publish-conventions")
}

yarn.lockFileName = "kotlin-yarn.lock"
yarn.lockFileDirectory = projectDir

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

kotlin {
    explicitApi()

    targets.configureEach {
        compilations.configureEach {
            compilerOptions.options.apiVersion = KotlinVersion.KOTLIN_1_9
            compilerOptions.options.languageVersion = KotlinVersion.KOTLIN_1_9
        }
    }


    js(IR) {
        browser()
        nodejs()
    }

    jvm {
        withJava()

        compilations.configureEach {
            compilerOptions.options.jvmTarget = JvmTarget.JVM_11
        }
    }

    androidNativeArm32()
    androidNativeArm64()
    androidNativeX64()
    androidNativeX86()

    linuxArm64()
    linuxX64()

    iosArm64()
    iosX64()

    iosSimulatorArm64()

    macosArm64()
    macosX64()

    mingwX64()

    tvosArm64()
    tvosX64()

    tvosSimulatorArm64()

    watchosArm32()
    watchosArm64()
    watchosX64()

    watchosDeviceArm64()
    watchosSimulatorArm64()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.serialization.core)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.release = 11
    }
}

publishing {
    publications {
        publications.withType<MavenPublication>().configureEach {
            val emptyJavadocJar = tasks.register<Jar>("${name}JavadocJar") {
                archiveBaseName = "${archiveBaseName.get()}-${name}"
                archiveClassifier = "javadoc"
            }

            artifact(emptyJavadocJar)
        }
    }
}