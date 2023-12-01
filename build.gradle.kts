/*
 * Copyright (c) 2022 Peter Csajtai
 * Copyright (c) 2023 Leon Linhart
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
                compileOnly(libs.kotlinx.serialization.core)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.serialization.json)
            }
        }

        nativeMain {
            dependencies {
                implementation(libs.kotlinx.serialization.core)
            }
        }
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.javaModuleVersion = "$version"
        options.release = 11
    }

    named<JavaCompile>("compileJava") {
        val files = named<KotlinCompile>("compileKotlinJvm").get().outputs.files

        options.compilerArgumentProviders += CommandLineArgumentProvider {
            listOf("--patch-module", "com.osmerion.kotlin.semver=${files.asPath}")
        }
    }

    withType<Jar>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true

        includeEmptyDirs = false
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