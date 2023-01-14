import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {
    implementation(project(":project:common"))
    implementation(project(":project:common-impl"))
    implementation(project(":project:module-bukkit"))
    implementation(project(":project:module-bungee"))
    implementation(project(":project:module-velocity"))
    implementation("com.eatthepath:fast-uuid:0.2.0")
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        exclude("META-INF/maven/**")
        exclude("META-INF/tf/**")
        exclude("module-info.java")

        relocate("com.eatthepath.uuid", "me.arasple.mc.trchat.library.uuid")
        relocate("taboolib", "me.arasple.mc.trchat.taboolib")
        relocate("kotlin.", "kotlin1721.") {
            exclude("kotlin.Metadata")
        }
    }
    build {
        dependsOn(shadowJar)
    }
}