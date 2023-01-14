val taboolibVersion: String by project

plugins {
    id("io.izzel.taboolib") version "1.55"
}

taboolib {
    install("common", "common-5")
    install("module-kether", "module-lang", "module-metrics")
    options("skip-minimize", "keep-kotlin-module", "skip-plugin-file")
    classifier = null
    version = taboolibVersion
}

dependencies {
    api(project(":project:common"))
}