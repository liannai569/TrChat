import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {
    implementation("net.kyori:adventure-api:4.11.0")
    implementation("net.kyori:adventure-platform-bukkit:4.1.1")
    implementation("net.kyori:adventure-platform-bungeecord:4.1.1")
    implementation(project(":project:common"))
    implementation(project(":project:implementation-common"))
    implementation(project(":project:implementation-bukkit"))
    implementation(project(":project:implementation-bungee"))
    implementation(project(":project:implementation-velocity"))
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("shaded")
        exclude("META-INF/maven/**")
        exclude("META-INF/tf/**")
        exclude("module-info.java")
    }
    build {
        dependsOn(shadowJar)
    }
}