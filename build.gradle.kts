plugins {
    id("org.gradle.java")
    id("org.gradle.maven-publish")
    id("org.jetbrains.kotlin.jvm") version "1.6.21" apply false
}

subprojects {
    apply<JavaPlugin>()
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
    }
    dependencies {
        "compileOnly"(kotlin("stdlib"))
    }
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

tasks.jar {
    onlyIf { false }
}

tasks.build {
    doLast {
        val plugin = project(":plugin")
        val file = file("${plugin.buildDir}/libs").listFiles()?.find { it.endsWith("plugin-$version.jar") }

        file?.copyTo(file("$buildDir/libs/${project.name}-$version.jar"), true)
    }
    dependsOn(project(":plugin").tasks.build)
}