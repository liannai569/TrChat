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
//        mavenLocal()
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

tasks.build {
    doFirst {
        buildDir.deleteRecursively()
    }
    doLast {
        val plugin = project(":plugin")
        val file = file("${plugin.buildDir}/libs").listFiles()?.find { it.endsWith("plugin-$version.jar") }

        file?.copyTo(file("$buildDir/libs/${project.name}-$version.jar"), true)

        val pluginShaded = project(":plugin-shaded")
        val fileShaded = file("${pluginShaded.buildDir}/libs").listFiles()?.find { it.endsWith("plugin-shaded-$version-shaded.jar") }

        fileShaded?.copyTo(file("$buildDir/libs/${project.name}-$version-shaded.jar"), true)

        val ver = file("$buildDir/libs/版本说明 Versions.txt")
        ver.createNewFile()
        ver.writeText("""
            1.16.5及以上的paper或分支: 必须使用普通版本
            其他情况下二者皆可
            若低版本与其他插件依赖冲突, 使用shaded版本
            
            paper and its forks above 1.16.5: must use common version.
            if conflict occurred with other plugins in lower minecraft versions, use shaded version.
        """.trimIndent())
    }
    dependsOn(project(":plugin").tasks.build, project(":plugin-shaded").tasks.build)
}