val taboolibVersion: String by project

plugins {
    id("io.izzel.taboolib") version "1.40"
}

taboolib {
    description {
        name(rootProject.name)
        desc("Advanced Minecraft Chat Control")
        contributors {
            name("Arasple")
            name("ItsFlicker").description("Maintainer")
        }
    }
    install(
        "common",
        "common-5",
        "module-chat",
        "module-configuration",
        "module-lang",
        "module-metrics",
        "platform-bungee"
    )
    options("skip-minimize", "keep-kotlin-module")
    classifier = null
    version = taboolibVersion
}

repositories {
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    api(project(":project:common"))

    compileOnly("net.kyori:adventure-api:4.11.0")
    compileOnly("net.kyori:adventure-platform-bungeecord:4.1.1")
    compileOnly("net.md-5:bungeecord-api:1.16-R0.5-SNAPSHOT")
}