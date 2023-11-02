plugins {
    id("com.osmerion.base-conventions")
    `maven-publish`
    signing
}

publishing {
    repositories {
        val sonatypeUsername: String? by project
        val sonatypePassword: String? by project
        val stagingRepositoryId: String? by project

        if (sonatypeUsername != null && sonatypePassword != null && stagingRepositoryId != null) {
            maven {
                url = uri("https://s01.oss.sonatype.org/service/local/staging/deployByRepositoryId/$stagingRepositoryId/")

                credentials {
                    username = sonatypeUsername
                    password = sonatypePassword
                }
            }
        }
    }
    publications.withType<MavenPublication>().configureEach {
        pom {
            name = project.name
            url = "https://github.com/Osmerion/gradle-liquibase"
            packaging = "jar"

            licenses {
                license {
                    name = "Apache-2.0"
                    url = "https://github.com/Osmerion/gradle-liquibase/blob/master/LICENSE"
                    distribution = "repo"
                }
            }

            developers {
                developer {
                    id = "TheMrMilchmann"
                    name = "Leon Linhart"
                    email = "themrmilchmann@gmail.com"
                    url = "https://github.com/TheMrMilchmann"
                }
            }

            scm {
                connection = "scm:git:git://github.com/Osmerion/gradle-liquibase.git"
                developerConnection = "scm:git:git://github.com/Osmerion/gradle-liquibase.git"
                url = "https://github.com/Osmerion/gradle-liquibase.git"
            }
        }
    }
}

signing {
    // Only require signing when publishing to a non-local maven repository
    setRequired { gradle.taskGraph.allTasks.any { it is PublishToMavenRepository } }

    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)

    sign(publishing.publications)
}