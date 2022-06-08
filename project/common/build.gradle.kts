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
        "common-5"
    )
    classifier = null
    version = taboolibVersion
}

dependencies {
    compileOnly("com.google.code.gson:gson:2.9.0")
    compileOnly("com.google.guava:guava:23.0")
}

tasks.tabooRelocateJar {
    onlyIf { false }
}