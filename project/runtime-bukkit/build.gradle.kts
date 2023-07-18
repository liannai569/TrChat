val taboolib_version: String by project

plugins {
    id("io.izzel.taboolib") version "1.56"
}

dependencies {
    compileOnly(project(":project:common"))
    compileOnly(project(":project:module-adventure"))
    compileOnly(project(":project:module-nms"))
    compileOnly("ink.ptms.core:v12001:12001:universal")
    compileOnly("net.md-5:bungeecord-api:1.20-R0.1-SNAPSHOT")

    compileOnly("me.clip:placeholderapi:2.11.3") { isTransitive = false }
    compileOnly("com.willfp:eco:6.35.1") { isTransitive = false }
    compileOnly("com.github.LoneDev6:api-itemsadder:3.5.0b") { isTransitive = false }
    compileOnly("xyz.xenondevs.nova:nova-api:0.12.13") { isTransitive = false }
}

taboolib {
    description {
        name(rootProject.name)
        desc("Advanced Minecraft Chat Control")
        contributors {
            name("Arasple")
            name("ItsFlicker")
        }
        dependencies {
            name("PlaceholderAPI").with("bukkit")
            name("EcoEnchants").with("bukkit").optional(true)
            name("ItemsAdder").with("bukkit").optional(true)
            name("Nova").with("bukkit").optional(true)
            name("Multiverse-Core").loadafter(true)
        }
    }
    install("common", "platform-bukkit")
    options("skip-minimize", "keep-kotlin-module", "skip-taboolib-relocate")
    classifier = null
    version = taboolib_version
}