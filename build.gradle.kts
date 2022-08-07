plugins {
    id("org.gradle.java")
    id("org.gradle.maven-publish")
    id("org.jetbrains.kotlin.jvm") version "1.5.10" apply false
}

subprojects {
    apply<JavaPlugin>()
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
    }
    dependencies {
        compileOnly("com.google.code.gson:gson:2.8.5")
        compileOnly("com.google.guava:guava:21.0")
        compileOnly("net.kyori:adventure-api:4.11.0")
        compileOnly(kotlin("stdlib"))
    }
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

gradle.buildFinished {
    buildDir.deleteRecursively()
}

tasks.build {
    doLast {
        val plugin = project(":plugin")
        val file = file("${plugin.buildDir}/libs").listFiles()?.find { it.endsWith("plugin-$version.jar") }

        file?.renameTo(file("${plugin.buildDir}/libs/${project.name}-$version.jar"))

        val pluginShaded = project(":plugin-shaded")
        val fileShaded = file("${pluginShaded.buildDir}/libs").listFiles()?.find { it.endsWith("plugin-shaded-$version-shaded.jar") }

        fileShaded?.renameTo(file("${pluginShaded.buildDir}/libs/${project.name}-$version-shaded.jar"))
    }
    dependsOn(project(":plugin").tasks.build, project(":plugin-shaded").tasks.build)
}