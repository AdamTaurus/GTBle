import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

group = "com.github.AdamTaurus.GTBle"
version = System.getenv("VERSION") ?: "local-SNAPSHOT"

android {
    namespace = "com.goolton.content_sdk"
    compileSdk = 34

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("com.google.code.gson:gson:2.10.1")
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                groupId = project.group.toString()
                artifactId = "content_sdk"
                version = project.version.toString()

                from(components["release"])

                pom {
                    name.set("content_sdk")
                    description.set("Android SDK demo for communicating with Content Center over AIDL.")
                    url.set("https://github.com/AdamTaurus/GTBle")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    developers {
                        developer {
                            id.set("AdamTaurus")
                            name.set("AdamTaurus")
                        }
                    }

                    scm {
                        connection.set("scm:git:https://github.com/AdamTaurus/GTBle.git")
                        developerConnection.set("scm:git:ssh://git@github.com/AdamTaurus/GTBle.git")
                        url.set("https://github.com/AdamTaurus/GTBle")
                    }
                }
            }
        }
    }
}
