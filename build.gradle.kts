import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.gradle.java")
    id("org.gradle.maven-publish")
    id("org.jetbrains.kotlin.jvm") version "1.5.31" apply false
}

subprojects {
    apply<JavaPlugin>()
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
    }
    dependencies {
        compileOnly(kotlin("stdlib"))
        compileOnly("com.google.code.gson:gson:2.8.5")
        compileOnly("com.google.guava:guava:21.0")
        compileOnly("net.kyori:adventure-api:4.11.0")
    }
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    tasks.withType<KotlinCompile> { kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }  }
    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

//gradle.buildFinished {
//    buildDir.deleteRecursively()
//}

tasks.build {
    doLast {
        val plugin = project(":plugin")
        val file = file("${plugin.buildDir}/libs").listFiles()?.find { it.endsWith("plugin-$version.jar") }

        file?.copyTo(file("$buildDir/libs/${project.name}-$version.jar"), true)
    }
    dependsOn(project(":plugin").tasks.build)
}