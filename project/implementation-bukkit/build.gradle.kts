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
        dependencies {
            name("PlaceholderAPI").with("bukkit")
            name("EcoEnchants").with("bukkit").optional(true)
            name("ItemsAdder").with("bukkit").optional(true)
            name("InteractiveChat").with("bukkit").optional(true)
        }
    }
    install(
        "common",
        "common-5",
        "module-chat",
        "module-configuration",
        "module-database",
        "module-database-mongodb",
        "module-kether",
        "module-lang",
        "module-metrics",
        "module-nms",
        "module-nms-util",
        "module-ui",
        "platform-bukkit",
        "expansion-command-helper",
        "expansion-javascript"
    )
    options("skip-minimize", "keep-kotlin-module")
    classifier = null
    version = taboolibVersion
}

repositories {
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    compileOnly(project(":project:common"))

    compileOnly("net.kyori:adventure-platform-bukkit:4.1.1")

    compileOnly("ink.ptms.core:v11900:11900-minimize:mapped")
    compileOnly("ink.ptms.core:v11900:11900-minimize:universal")
    compileOnly("ink.ptms:nms-all:1.0.0")
    compileOnly("net.md-5:bungeecord-api:1.19-R0.1-SNAPSHOT")

    compileOnly("com.willfp:eco:6.6.3") { isTransitive = false }
    compileOnly("me.clip:placeholderapi:2.11.1") { isTransitive = false }

    compileOnly(fileTree("libs"))
}