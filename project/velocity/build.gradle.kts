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
        "platform-velocity",
        "expansion-command-helper",
    )
    classifier = null
    version = taboolibVersion
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    api(project(":project:common"))
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    compileOnly(fileTree("libs"))
}