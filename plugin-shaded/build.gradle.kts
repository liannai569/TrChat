import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {
    implementation("com.eatthepath:fast-uuid:0.2.0")
    implementation("net.kyori:adventure-api:4.11.0")
    implementation("net.kyori:adventure-platform-bukkit:4.1.1")
    implementation("net.kyori:adventure-platform-bungeecord:4.1.1")
    implementation(project(":project:common"))
    implementation(project(":project:implementation-common"))
    implementation(project(":project:implementation-bukkit"))
    implementation(project(":project:implementation-bungee"))
//    implementation(project(":project:implementation-velocity"))
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("shaded")
        exclude("META-INF/maven/**")
        exclude("META-INF/tf/**")
        exclude("module-info.java")
        relocate("com.eatthepath.uuid", "me.arasple.mc.trchat.library.uuid")
        relocate("net.kyori.adventure", "me.arasple.mc.trchat.library.adventure")
        relocate("net.kyori.examination", "me.arasple.mc.trchat.library.examination")
    }
    build {
        dependsOn(shadowJar)
    }
}