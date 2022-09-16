val taboolibVersion: String by project

plugins {
    id("io.izzel.taboolib") version "1.42"
}

taboolib {
    install("common", "common-5")
    install("module-lang", "module-metrics")
    options("skip-minimize", "keep-kotlin-module", "skip-plugin-file")
    classifier = null
    version = taboolibVersion
}

dependencies {
    compileOnly("com.eatthepath:fast-uuid:0.2.0")
    compileOnly("net.kyori:adventure-platform-api:4.1.2")
}