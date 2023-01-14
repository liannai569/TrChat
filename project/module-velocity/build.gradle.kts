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
        "platform-velocity"
    )
    options("skip-minimize", "keep-kotlin-module")
    classifier = null
    version = taboolibVersion
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    api(project(":project:common"))
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
}