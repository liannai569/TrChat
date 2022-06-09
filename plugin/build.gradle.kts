import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {
    implementation(project(":project:common"))
    implementation(project(":project:bukkit"))
    implementation(project(":project:bungee"))
    implementation(project(":project:velocity"))
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        relocate("taboolib", "${project.group}.taboolib")
    }
    build {
        dependsOn(shadowJar)
    }
}