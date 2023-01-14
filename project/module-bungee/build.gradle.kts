val taboolibVersion: String by project

plugins {
    id("io.izzel.taboolib") version "1.55"
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
        "platform-bungee"
    )
    options("skip-minimize", "keep-kotlin-module")
    classifier = null
    version = taboolibVersion
}

repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    api(project(":project:common"))
    compileOnly("net.kyori:adventure-platform-bungeecord:4.2.0")
    compileOnly("net.md-5:bungeecord-api:1.19-R0.1-SNAPSHOT")
}