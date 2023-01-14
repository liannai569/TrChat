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
        dependencies {
            name("PlaceholderAPI").with("bukkit")
            name("EcoEnchants").with("bukkit").optional(true)
            name("ItemsAdder").with("bukkit").optional(true)
            name("InteractiveChat").with("bukkit").optional(true)
            name("Nova").with("bukkit").optional(true)
            name("Multiverse-Core").loadafter(true)
        }
    }
    install("common", "common-5")
    install(
        "module-chat",
        "module-configuration",
        "module-database",
        "module-kether",
        "module-lang",
        "module-nms",
        "module-nms-util",
        "module-ui"
    )
    install("platform-bukkit")
    install("expansion-alkaid-redis", "expansion-command-helper", "expansion-javascript")
    options("skip-minimize", "keep-kotlin-module")
    classifier = null
    version = taboolibVersion
}

repositories {
    maven("https://jitpack.io")
    maven("https://repo.xenondevs.xyz/releases")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    api(project(":project:common"))

    compileOnly("ink.ptms.core:v11900:11900:mapped")
    compileOnly("ink.ptms.core:v11900:11900:universal")
    compileOnly("ink.ptms.core:v11903:11903:mapped")
    compileOnly("ink.ptms:nms-all:1.0.0")
    compileOnly("net.md-5:bungeecord-api:1.19-R0.1-SNAPSHOT")

    compileOnly("net.kyori:adventure-text-minimessage:4.12.0")
    compileOnly("net.kyori:adventure-platform-bukkit:4.2.0")
    compileOnly("com.willfp:eco:6.35.1") { isTransitive = false }
    compileOnly("com.github.LoneDev6:api-itemsadder:3.2.5") { isTransitive = false }
    compileOnly("me.clip:placeholderapi:2.11.1") { isTransitive = false }
    compileOnly("xyz.xenondevs.nova:nova-api:0.12.13") { isTransitive = false }

    compileOnly(fileTree("libs"))
}